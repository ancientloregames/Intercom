package com.ancientlore.intercom.ui.chat.flow

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.text.Editable
import android.util.Log
import androidx.annotation.IntDef
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.*
import com.ancientlore.intercom.data.model.*
import com.ancientlore.intercom.data.model.Chat.Companion.TYPE_PRIVATE
import com.ancientlore.intercom.data.source.*
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.contact.detail.ContactDetailParams
import com.ancientlore.intercom.utils.Runnable1
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.createAudioMessageFile
import com.ancientlore.intercom.utils.extensions.isInternal
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import com.ancientlore.intercom.view.MessageInputManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.lang.ref.WeakReference

class ChatFlowViewModel(
	context: Context,
	private val params: ChatFlowParams,
)
	: FilterableViewModel<ChatFlowAdapter>(ChatFlowAdapter(params.userId, context)) {

	companion object {
		const val OPTION_AUDIO_CALL = 0
		const val OPTION_VIDEO_CALL = 1

		const val ITEM_OPTION_DELETE = 0

		const val ATTACH_OPTION_IMAGE = 0
		const val ATTACH_OPTION_FILE = 1

		const val TOAST_CHAT_CREATION_ERR = 0
		const val TOAST_MSG_SEND_ERR = 1
		const val TOAST_MSG_DELETED = 2
		const val TOAST_MSG_DELETED_NOT = 3
		const val TOAST_MSG_UNDELETABLE = 4
		const val TOAST_FILE_TOO_BIG = 5
	}

	@IntDef(OPTION_AUDIO_CALL, OPTION_VIDEO_CALL)
	@Retention(AnnotationRetention.SOURCE)
	annotation class Option

	@IntDef(ITEM_OPTION_DELETE)
	@Retention(AnnotationRetention.SOURCE)
	annotation class ItemOption

	@IntDef(ATTACH_OPTION_IMAGE, ATTACH_OPTION_FILE)
	@Retention(AnnotationRetention.SOURCE)
	annotation class AttachMenuOption

	val textField = ObservableField("")

	val actionBarTitleField = ObservableField(params.title)

	val actionBarSubtitleField = ObservableField("")

	val actionBarIconField = ObservableField(params.iconUri)

	val showSendProgressField = ObservableBoolean(false)

	val showScrollToBottom = ObservableBoolean(false)

	private val repository = MessageRepository()

	private var repositorySub: RepositorySubscription? = null

	private var contactRepSub: RepositorySubscription? = null

	private val messageText get() = textField.get()!!

	private val openAttachMenuSubj = PublishSubject.create<Any>()

	private val recordAudioSubj = PublishSubject.create<Any>()

	private val uploadIconSubj = PublishSubject.create<String>() // Chat Id

	private val openChatDetailSubj = PublishSubject.create<ChatFlowParams>()

	private val openContactDetailSubj = PublishSubject.create<ContactDetailParams>()

	private val makeAudioCallSubj = PublishSubject.create<CallViewModel.Params>()

	private val makeVideoCallSubj = PublishSubject.create<CallViewModel.Params>()

	private val setContactStatusOnlineSubj = PublishSubject.create<Any>()

	private val setContactStatusLastSeenSubj = PublishSubject.create<String>()

	private val srollToPositionSubj = PublishSubject.create<Int>()

	private val pickFileSubj = PublishSubject.create<EmptyObject>()

	private val pickImageSubj = PublishSubject.create<EmptyObject>()

	private val showSendButtonSubj = PublishSubject.create<Boolean>()

	private var inputManager: MessageInputManager? = null

	private var receiverId: String? = null

	private var paginationCompleted = false

	private var recordedAudioFileRef: WeakReference<File>? = null

	init {

		if (params.chatId.isNotEmpty()) {
			initMessageRepository(params.chatId)

			ChatRepository.setMessageRecieved(params.chatId)

			loadNextPage {

				attachRepositoryChangeListener()
			}
		}

		if (params.chatType == TYPE_PRIVATE) {
			val contactId = params.participants.first { it != params.userId }
			observeContactOnlineStatus(contactId)
		}
	}

	fun attachInputPanelManager(manager: MessageInputManager) {
		inputManager = manager
		manager.setListener(object : MessageInputManager.Listener {
			private var recorder: MediaRecorder? = null

			private var outFile: File? = null

			override fun onStarted() {
				outFile = App.context.createAudioMessageFile()
				outFile?.let { file ->
					try {
						recorder = MediaRecorder().apply {
							setAudioSource(MediaRecorder.AudioSource.MIC)
							setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
							setOutputFile(file.absolutePath)
							setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
							prepare()
							start()
						}
					} catch (e: IllegalStateException) {
						Utils.logError(e)
					}
				} ?: Utils.logError("Unable to create audio output file")
			}

			override fun onCompleted() {
				outFile?.let { file ->
					releaseRecorder()
					handleAudioMessage(file)
					outFile = null
				}
			}

			override fun onCompleteContinuousRecording() {
				outFile?.let { file ->
					releaseRecorder()
					recordedAudioFileRef = WeakReference(file)
				}
			}

			override fun onCanceled() {
				releaseRecorder()
				outFile?.run {
					manager.onMessageSent()
					delete()
					outFile = null
				}
			}

			private fun releaseRecorder() {
				recorder?.run {
					try {
						stop()
						reset()
						release()
						recorder = null
					} catch (e: RuntimeException) {
						Utils.logError(e)
					}
				}
			}
		})
	}

	override fun clean() {
		if (repository.getSourceId().isNotEmpty())
			ChatRepository.setMessageRecieved(repository.getSourceId())

		listAdapter.clean()

		openAttachMenuSubj.onComplete()
		recordAudioSubj.onComplete()
		uploadIconSubj.onComplete()
		openChatDetailSubj.onComplete()
		openContactDetailSubj.onComplete()
		makeAudioCallSubj.onComplete()
		makeVideoCallSubj.onComplete()
		setContactStatusOnlineSubj.onComplete()
		setContactStatusLastSeenSubj.onComplete()
		srollToPositionSubj.onComplete()
		pickFileSubj.onComplete()
		pickImageSubj.onComplete()

		inputManager?.dispose()
		inputManager = null
		contactRepSub?.remove()
		repositorySub?.remove()

		super.clean()
	}

	// ----------- Rx Observables -----------

	fun openAttachmentMenuRequest() = openAttachMenuSubj as Observable<Any>

	fun recordAudioRequest() = recordAudioSubj as Observable<Any>

	fun uploadIconRequest() = uploadIconSubj as Observable<String>

	fun openChatDetailRequest() = openChatDetailSubj as Observable<ChatFlowParams>

	fun openContactDetailRequest() = openContactDetailSubj as Observable<ContactDetailParams>

	fun makeAudioCallRequest() = makeAudioCallSubj as Observable<CallViewModel.Params>

	fun makeVideoCallRequest() = makeVideoCallSubj as Observable<CallViewModel.Params>

	fun setContactStatusOnlineRequest() = setContactStatusOnlineSubj as Observable<Any>

	fun setContactStatusLastSeenRequest() = setContactStatusLastSeenSubj as Observable<String>

	fun scrollToPositionRequest() = srollToPositionSubj as Observable<Int>

	fun pickFileRequest() = pickFileSubj as Observable<Any>

	fun pickImageRequest() = pickImageSubj as Observable<Any>

	fun showSendButtonRequest() = showSendButtonSubj as Observable<Boolean>

	// ----------- DataBinding Events -----------

	fun onInputTextChanged(input: Editable) {
		showSendButtonSubj.onNext(input.isNotEmpty())
	}

	fun onAttachButtonClicked() = openAttachMenuSubj.onNext(EmptyObject)

	fun onActionBarCliked() { // TODO really need to separate private and group chats
		if (params.chatType == TYPE_PRIVATE)
			openContactDetailSubj.onNext(ContactDetailParams(
				receiverId!!,
				params.title,
				params.iconUri.toString(),
				true))
		else
			openChatDetailSubj.onNext(params)
	}

	fun onSendButtonClicked() {
		recordedAudioFileRef?.get()
			?.let {
				recordedAudioFileRef = null
				handleAudioMessage(it)
			}
			?: messageText
					.takeIf { it.isNotBlank() }
					?.let {
						sendMessage(it)
						textField.set("")
					}
	}

	fun onRecordButtonClicked() = recordAudioSubj.onNext(EmptyObject)

	fun onStopRecordButtonClicked() {
		inputManager?.onStop()
	}

	fun onCancelRecordButtonClicked() {
		inputManager?.onCancel()
	}

	fun onSrollToBottomClicked() = srollToPositionSubj.onNext(listAdapter.itemCount - 1)

	// ----------- Repository interactions (messages sending) -----------

	private fun sendMessage(text: String) {
		guarantyChat {
			showSendProgressField.set(true)
			val message = Message(
				senderId = params.userId,
				text = text,
				receivers = params.participants)
			repository.addItem(message, object : RequestCallback<String> {
				override fun onSuccess(result: String) {
					runOnUiThread {
						showSendProgressField.set(false)
					}
				}
				override fun onFailure(error: Throwable) {
					onFailureSendingMessage(error)
				}
			})
		}
	}

	fun handleAttachedImage(fileData: FileData, conpressed: Uri) {
		guarantyChat { chatId ->
			showSendProgressField.set(true)
			val message = Message(
				senderId = params.userId,
				attachUrl = conpressed.toString(),
				type = Message.TYPE_IMAGE,
				receivers = params.participants)
			repository.addItem(message, object : RequestCallback<String> {
				override fun onSuccess(messageId: String) {
					App.backend.getStorageManager().uploadImage(fileData, chatId, object : ProgressRequestCallback<Uri> {
						override fun onProgress(progress: Int) {
							runOnUiThread {
								listAdapter.setFileUploadProgress(messageId, progress)
							}
						}
						override fun onSuccess(uri: Uri) {
							repository.updateMessageUri(messageId, uri, object : RequestCallback<Any> {
								override fun onSuccess(result: Any) {
									runOnUiThread {
										showSendProgressField.set(false)
									}
								}
								override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
							})
						}
						override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
					})
				}
				override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
			})
		}
	}

	fun handleAttachedFile(fileData: FileData) {

		if (fileData.size > C.MAX_ATTACH_FILE_SIZE) {
			toastRequest.onNext(TOAST_FILE_TOO_BIG)
			return
		}

		guarantyChat { chatId ->
			showSendProgressField.set(true)

			val message = Message(
				senderId = params.userId,
				text = fileData.name,
				info = fileData.getInfo(),
				attachUrl = fileData.uri.toString(),
				type = Message.TYPE_FILE,
				receivers = params.participants)
			repository.addItem(message, object : RequestCallback<String> {

				override fun onSuccess(messageId: String) {

					App.backend.getStorageManager().uploadFile(fileData, chatId, object : ProgressRequestCallback<Uri> {
						override fun onProgress(progress: Int) {
							runOnUiThread {
								listAdapter.setFileUploadProgress(messageId, progress)
							}
						}
						override fun onSuccess(result: Uri) {
							repository.updateMessageUri(messageId, result, object : RequestCallback<Any> {
								override fun onSuccess(result: Any) {
									runOnUiThread {
										showSendProgressField.set(false)
									}
								}
								override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
							})
						}
						override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
					})
				}
				override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
			})
		}
	}

	fun handleAudioMessage(file: File) {
		guarantyChat { chatId ->
			showSendProgressField.set(true)

			val message = Message(
				senderId = params.userId,
				attachUrl = file.name,
				type = Message.TYPE_AUDIO,
				receivers = params.participants)
			repository.addItem(message, object : RequestCallback<String> {

				override fun onSuccess(messageId: String) {

					val uri = Uri.fromFile(file)
					App.backend.getStorageManager().uploadAudioMessage(uri, chatId, object : ProgressRequestCallback<Uri> {

						override fun onProgress(progress: Int) {
							runOnUiThread {
								listAdapter.setFileUploadProgress(messageId, progress)
							}
						}
						override fun onSuccess(result: Uri) {

							repository.updateMessageUri(messageId, result, object : RequestCallback<Any> {

								override fun onSuccess(result: Any) {
									runOnUiThread {
										showSendProgressField.set(false)
										inputManager?.onMessageSent()
									}
								}
								override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
							})
						}
						override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
					})
				}
				override fun onFailure(error: Throwable) { onFailureSendingMessage(error) }
			})
		}
	}

	private fun deleteMessage(message: Message) {

		if (message.undeletable.not()) {
			repository.deleteItem(message.id, object : CrashlyticsRequestCallback<Any>() {
				override fun onSuccess(result: Any) {
					toastRequest.onNext(TOAST_MSG_DELETED)
				}
				override fun onFailure(error: Throwable) {
					super.onFailure(error)
					toastRequest.onNext(TOAST_MSG_DELETED_NOT)
				}
			})
		}
		else {
			toastRequest.onNext(TOAST_MSG_UNDELETABLE)
		}
	}

	// ----------- Ui events handling -----------

	fun onLastVisibleItemChanged(lastVisibleItemPos: Int) {
		val show = lastVisibleItemPos < listAdapter.itemCount - 1
		val currentState = showScrollToBottom.get()!!
		if (currentState != show)
			showScrollToBottom.set(show)
	}

	fun onScrolledToTop() {
		if (paginationCompleted.not()) {
			listAdapter.onPaginationStart(true)
			loadNextPage()
		}
	}

	fun onOptionSelected(@Option selectedId: Int) {
		when (selectedId) {
			OPTION_AUDIO_CALL -> makeAudioCallSubj.onNext(
				CallViewModel.Params(
					receiverId!!,
					params.title,
					params.iconUri.toString()
				))
			OPTION_VIDEO_CALL -> makeVideoCallSubj.onNext(
				CallViewModel.Params(
					receiverId!!,
					params.title,
					params.iconUri.toString()
				))
		}
	}

	fun onContactStatusChanged(status: String) {
		runOnUiThread {
			actionBarSubtitleField.set(status)
		}
	}

	fun onMessageMenuOptionSelected(message: Message, @ItemOption id: Int) {
		when (id) {
			ITEM_OPTION_DELETE -> deleteMessage(message)
		}
	}

	fun onAttachMenuOptionSelected(@AttachMenuOption id: Int) {
		when (id) {
			ATTACH_OPTION_FILE -> pickFileSubj.onNext(EmptyObject)
			ATTACH_OPTION_IMAGE -> pickImageSubj.onNext(EmptyObject)
		}
	}

	// ----------- Private logics -----------

	private fun onFailureSendingMessage(error: Throwable) {
		if (error !is EmptyResultException)
			Utils.logError(error)
		runOnUiThread {
			showSendProgressField.set(false)
			inputManager?.onMessageSent()
		}
		toastRequest.onNext(TOAST_MSG_SEND_ERR)
	}

	private fun guarantyChat(callback: Runnable1<String>) {

		if (repository.getSourceId().isEmpty()) {

			val chat = Chat(
				name = params.title,
				iconUrl = params.iconUri.toString(),
				initiatorId = params.userId,
				participants = params.participants,
				type = params.chatType,
				pin = false,
				mute = false)

			ChatRepository.addItem(chat, object : RequestCallback<String> {
				override fun onSuccess(id: String) {
					initMessageRepository(id)
					attachRepositoryChangeListener()

					if (params.iconUri.isInternal())
						uploadIconSubj.onNext(id)

					runOnUiThread {
						callback.run(id)
					}
				}
				override fun onFailure(error: Throwable) {
					Utils.logError(error)
					toastRequest.onNext(TOAST_CHAT_CREATION_ERR)
				}
			})
		}
		else callback.run(repository.getSourceId())
	}

	private fun initMessageRepository(chatId: String) {
		repository.apply {
			setRemoteSource(App.backend.getDataSourceProvider().getMessageSource(chatId))
			//setLocalSource(App.frontend.getDataSourceProvider().getMessageSource(chatId))
		}
	}

	private fun attachRepositoryChangeListener() {

		repositorySub = repository.attachChangeListener(object : RequestCallback<ListChanges<Message>> {
			override fun onSuccess(result: ListChanges<Message>) {
				runOnUiThread {
					listAdapter.applyChanges(result)
				}
				updateMessagesStatus(result.addList)
			}
			override fun onFailure(error: Throwable) {
				if (error !is EmptyResultException)
					Utils.logError(error)
			}
		})
	}

	private fun updateMessagesStatus(messages: List<Message>) {
		messages
			.filter { it.status != Message.STATUS_RECEIVED && it.senderId != params.userId }
			.forEach {
				Log.d(C.DEFAULT_LOG_TAG, "updateMessagesStatus ${it.text}")
				repository.setMessageStatusReceived(it.id)
			}
	}

	private fun observeContactOnlineStatus(contactId: String) {
		this.receiverId = contactId

		contactRepSub = UserRepository.attachListener(contactId, object : CrashlyticsRequestCallback<User>() {

			override fun onSuccess(collocutor: User) {
				if (collocutor.online)
					setContactStatusOnlineSubj.onNext(EmptyObject)
				else
					setContactStatusLastSeenSubj.onNext(collocutor.lastSeenDate)
				}
		})
	}

	private fun loadNextPage(onLoaded: Runnable? = null) {

		repository.getNextPage(object: CrashlyticsRequestCallback<List<Message>>() {

			override fun onSuccess(result: List<Message>) {
				runOnUiThread {
					if (result.isNotEmpty()) {
						if (result.size < C.DEF_MSG_PAGINATION_LIMIT)
							paginationCompleted = true

						listAdapter.prependItems(result)
					}
					else
						paginationCompleted = true
					listAdapter.onPaginationStart(false)
				}
				updateMessagesStatus(result)

				onLoaded?.run()
			}
		})
	}
}

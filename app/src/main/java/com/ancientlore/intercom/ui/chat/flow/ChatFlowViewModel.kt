package com.ancientlore.intercom.ui.chat.flow

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import androidx.annotation.IntDef
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.R
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

class ChatFlowViewModel(listAdapter: ChatFlowAdapter,
                        private val params: ChatFlowParams)
	: FilterableViewModel<ChatFlowAdapter>(listAdapter) {

	companion object {
		const val OPTION_AUDIO_CALL = 0
		const val OPTION_VIDEO_CALL = 1
	}

	@IntDef(OPTION_AUDIO_CALL, OPTION_VIDEO_CALL)
	@Retention(AnnotationRetention.SOURCE)
	annotation class Option

	val textField = ObservableField("")

	val actionBarTitleField = ObservableField(params.title)

	val actionBarSubtitleField = ObservableField("")

	val actionBarIconField = ObservableField(params.iconUri)

	val showSendProgressField = ObservableBoolean(false)

	private val repository = MessageRepository()

	private var repositorySub: RepositorySubscription? = null

	private val messageText get() = textField.get()!!

	private val openAttachMenuSubj = PublishSubject.create<Any>()

	private val recordAudioSubj = PublishSubject.create<Any>()

	private val uploadIconSub = PublishSubject.create<String>() // Chat Id

	private val openChatDetailSub = PublishSubject.create<ChatFlowParams>()

	private val openContactDetailSubj = PublishSubject.create<ContactDetailParams>()

	private val makeAudioCallSubj = PublishSubject.create<CallViewModel.Params>()

	private val makeVideoCallSubj = PublishSubject.create<CallViewModel.Params>()

	private var contactRepSub: RepositorySubscription? = null

	private var inputManager: MessageInputManager? = null

	private var receiverId: String? = null

	private var paginationCompleted = false

	fun init(context: Context) {

		if (params.chatId.isNotEmpty()) {
			initMessageRepository(params.chatId)
			loadNextPage {

				attachRepositoryChangeListener()
			}
		}

		if (params.chatType == TYPE_PRIVATE) {
			val contactId = params.participants.first { it != params.userId }
			observeContactOnlineStatus(context, contactId)
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
				}
			}

			override fun onCanceled() {
				releaseRecorder()
				outFile?.delete()
			}

			private fun releaseRecorder() {
				if (recorder != null) {
					try {
						recorder!!.stop()
						recorder!!.release()
						recorder = null
					} catch (e: RuntimeException) {
						Utils.logError(e)
					}
				}
			}
		})
	}

	override fun clean() {
		openAttachMenuSubj.onComplete()
		recordAudioSubj.onComplete()
		uploadIconSub.onComplete()
		openChatDetailSub.onComplete()
		openContactDetailSubj.onComplete()
		makeAudioCallSubj.onComplete()
		makeVideoCallSubj.onComplete()
		inputManager?.onStop()
		inputManager?.setListener(null)
		inputManager = null
		contactRepSub?.remove()
		repositorySub?.remove()

		super.clean()
	}

	fun observeAttachMenuOpen() = openAttachMenuSubj as Observable<Any>

	fun observeAudioRecord() = recordAudioSubj as Observable<Any>

	fun observeUploadIcon() = uploadIconSub as Observable<String>

	fun observeOpenChatDetail() = openChatDetailSub as Observable<ChatFlowParams>

	fun observeOpenContactDetail() = openContactDetailSubj as Observable<ContactDetailParams>

	fun onAttachButtonCliked() = openAttachMenuSubj.onNext(EmptyObject)

	fun observeMakeAudioCallRequest() = makeAudioCallSubj as Observable<CallViewModel.Params>

	fun observeMakeVideoCallRequest() = makeVideoCallSubj as Observable<CallViewModel.Params>

	fun onActionBarCliked() { // TODO really need to separate private and group chats
		if (params.chatType == TYPE_PRIVATE)
			openContactDetailSubj.onNext(ContactDetailParams(
				receiverId!!,
				params.title,
				params.iconUri.toString(),
				true))
		else
			openChatDetailSub.onNext(params)
	}

	fun onSendButtonClicked() {
		messageText.takeIf { it.isNotBlank() }
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

	fun handleDelete(message: Message) {
		repository.deleteItem(message.id, object : CrashlyticsRequestCallback<Any>() {
			override fun onSuccess(result: Any) {
				toastRequest.onNext(R.string.message_deleted)
			}
		})
	}

	fun onScrolledToTop() {
		if (paginationCompleted.not())
			loadNextPage()
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

	private fun onFailureSendingMessage(error: Throwable) {
		if (error !is EmptyResultException)
			Utils.logError(error)
		runOnUiThread {
			showSendProgressField.set(false)
		}
		toastRequest.onNext(R.string.alert_error_send_message)
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
						uploadIconSub.onNext(id)

					runOnUiThread {
						callback.run(id)
					}
				}
				override fun onFailure(error: Throwable) {
					Utils.logError(error)
					toastRequest.onNext(R.string.alert_error_creating_chat)
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

	private fun observeContactOnlineStatus(context: Context, contactId: String) {
		this.receiverId = contactId

		contactRepSub = UserRepository.attachListener(contactId, object : CrashlyticsRequestCallback<User>() {

			override fun onSuccess(conterpart: User) {
				runOnUiThread {
					if (conterpart.online)
						actionBarSubtitleField.set(context.getString(R.string.online))
					else
						actionBarSubtitleField.set(context.getString(R.string.last_seen,
							conterpart.lastSeenDate))
				}
			}
		})
	}

	private fun loadNextPage(onLoaded: Runnable? = null) {

		repository.getNextPage(object: CrashlyticsRequestCallback<List<Message>>() {

			override fun onSuccess(result: List<Message>) {
				runOnUiThread {
					if (result.isNotEmpty())
						listAdapter.prependItems(result)
				}
				updateMessagesStatus(result)

				onLoaded?.run()
			}
		})
	}
}

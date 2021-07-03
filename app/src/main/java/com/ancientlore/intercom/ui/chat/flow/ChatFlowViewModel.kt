package com.ancientlore.intercom.ui.chat.flow

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.*
import com.ancientlore.intercom.data.model.*
import com.ancientlore.intercom.data.model.Chat.Companion.TYPE_PRIVATE
import com.ancientlore.intercom.data.source.*
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.utils.Runnable1
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.createAudioMessageFile
import com.ancientlore.intercom.utils.extensions.isInternal
import com.ancientlore.intercom.view.MessageInputManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File

class ChatFlowViewModel(listAdapter: ChatFlowAdapter,
                        private val params: ChatFlowParams)
	: FilterableViewModel<ChatFlowAdapter>(listAdapter) {

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

	private val openChatDetailSub = PublishSubject.create<Any>()

	private var contactRepSub: RepositorySubscription? = null

	private var inputManager: MessageInputManager? = null

	fun init(context: Context) {
		when {
			params.chatId.isNotEmpty() -> {
				initMessageRepository(params.chatId)

				/*FIXME should reorganized db structure. separate chat description and chat messages*/
				if (params.chatType == TYPE_PRIVATE) {
					val contactId = params.participants.first { it != params.userId }
					observeContactOnlineStatus(context, contactId)
				}
			}
			params.chatType == TYPE_PRIVATE -> {
				val contactId = params.participants.first { it != params.userId }
				ChatRepository.getItem(contactId, object : CrashlyticsRequestCallback<Chat>() {
					override fun onSuccess(chat: Chat) {
						initMessageRepository(chat.id)
					}
				})
				observeContactOnlineStatus(context, contactId)
			}
			params.participants.size > 2 -> {
			}
			else -> {
				val error = RuntimeException("Chat Flow Ui has been opened with no chat neither contacts ids")
				Utils.logError(error)
				throw error
			}
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
		inputManager?.onStop()
		contactRepSub?.remove()
		repositorySub?.remove()

		super.clean()
	}

	fun observeAttachMenuOpen() = openAttachMenuSubj as Observable<Any>

	fun observeAudioRecord() = recordAudioSubj as Observable<Any>

	fun observeUploadIcon() = uploadIconSub as Observable<String>

	fun onAttachButtonCliked() = openAttachMenuSubj.onNext(EmptyObject)

	fun onActionBarCliked() = openChatDetailSub.onNext(EmptyObject)

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
			val message = Message(senderId = params.userId, text = text)
			repository.addMessage(message, object : RequestCallback<String> {
				override fun onSuccess(result: String) {
					showSendProgressField.set(false)
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
				type = Message.TYPE_IMAGE)
			repository.addMessage(message, object : RequestCallback<String> {
				override fun onSuccess(messageId: String) {
					App.backend.getStorageManager().uploadImage(fileData, chatId, object : ProgressRequestCallback<Uri> {
						override fun onProgress(progress: Int) {
							listAdapter.setFileUploadProgress(messageId, progress)
						}
						override fun onSuccess(uri: Uri) {
							repository.updateMessageUri(messageId, uri, object : RequestCallback<Any> {
								override fun onSuccess(result: Any) {
									showSendProgressField.set(false)
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

			val message = Message(params.userId, fileData)
			repository.addMessage(message, object : RequestCallback<String> {

				override fun onSuccess(messageId: String) {

					App.backend.getStorageManager().uploadFile(fileData, chatId, object : ProgressRequestCallback<Uri> {
						override fun onProgress(progress: Int) {
							listAdapter.setFileUploadProgress(messageId, progress)
						}
						override fun onSuccess(result: Uri) {
							repository.updateMessageUri(messageId, result, object : RequestCallback<Any> {
								override fun onSuccess(result: Any) {
									showSendProgressField.set(false)
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

			val message = Message.createFromAudio(params.userId, file.name)
			repository.addMessage(message, object : RequestCallback<String> {

				override fun onSuccess(messageId: String) {

					val uri = Uri.fromFile(file)
					App.backend.getStorageManager().uploadAudioMessage(uri, chatId, object : ProgressRequestCallback<Uri> {

						override fun onProgress(progress: Int) {
							listAdapter.setFileUploadProgress(messageId, progress)
						}
						override fun onSuccess(result: Uri) {

							repository.updateMessageUri(messageId, result, object : RequestCallback<Any> {

								override fun onSuccess(result: Any) {
									showSendProgressField.set(false)
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
		repository.deleteMessage(message.id, object : CrashlyticsRequestCallback<Any>() {
			override fun onSuccess(result: Any) {
				toastRequest.onNext(R.string.message_deleted)
			}
		})
	}

	private fun onFailureSendingMessage(error: Throwable) {
		if (error !is EmptyResultException)
			Utils.logError(error)
		showSendProgressField.set(false)
		toastRequest.onNext(R.string.alert_error_send_message)
	}

	private fun guarantyChat(callback: Runnable1<String>) {

		if (repository.getChatId() == null) {

			val chat = Chat(
				name = params.title,
				iconUrl = params.iconUri.toString(),
				initiatorId = params.userId,
				participants = params.participants,
				type = if (params.participants.size == 2) Chat.TYPE_PRIVATE else Chat.TYPE_GROUP,
				pin = false,
				mute = false)

			ChatRepository.addItem(chat, object : RequestCallback<String> {
				override fun onSuccess(id: String) {
					initMessageRepository(id)

					if (params.iconUri.isInternal())
						uploadIconSub.onNext(id)

					callback.run(id)
				}
				override fun onFailure(error: Throwable) {
					Utils.logError(error)
					toastRequest.onNext(R.string.alert_error_creating_chat)
				}
			})
		}
		else callback.run(repository.getChatId())
	}

	private fun initMessageRepository(chatId: String) {
		repository.setRemoteSource(
			App.backend.getDataSourceProvider()
				.getMessageSource(chatId))

		repositorySub = repository.attachListener(object : RequestCallback<List<Message>>{
			override fun onSuccess(result: List<Message>) {
				listAdapter.setItems(result)
				updateMessagesStatus(result)
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
				repository.setMessageStatusReceived(it.id)
			}
	}

	private fun observeContactOnlineStatus(context: Context, contactId: String) {
		contactRepSub = UserRepository.attachListener(contactId, object : CrashlyticsRequestCallback<User>() {

			override fun onSuccess(conterpart: User) {
				if (conterpart.online)
					actionBarSubtitleField.set(context.getString(R.string.online))
				else
					actionBarSubtitleField.set(context.getString(R.string.last_seen,
						conterpart.lastSeenDate))
			}
		})
	}
}

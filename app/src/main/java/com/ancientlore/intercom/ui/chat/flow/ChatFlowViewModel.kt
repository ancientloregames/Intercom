package com.ancientlore.intercom.ui.chat.flow

import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.ProgressRequestCallback
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.FileData
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.createAudioMessageFile
import com.ancientlore.intercom.view.MessageInputManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File

class ChatFlowViewModel(private val userId: String,
                        private val chatId: String)
	: BasicViewModel() {

	val textField = ObservableField("")

	private lateinit var listAdapter: ChatFlowAdapter

	private val repository = MessageRepository()

	private val messageText get() = textField.get()!!

	private val openAttachMenuSubj = PublishSubject.create<Any>()

	private val recordAudioSubj = PublishSubject.create<Any>()

	private var inputManager: MessageInputManager? = null

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
						e.printStackTrace()
					}
				} ?: Log.e("ChatFlow", "Unable to create audio output file")
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
						e.printStackTrace()
					}
				}
			}
		})
	}

	init {
		val dataSourceProvider = App.backend.getDataSourceProvider()
		repository.setRemoteSource(dataSourceProvider.getMessageSource(chatId))
	}

	override fun onCleared() {
		super.onCleared()
		detachDataListener()
	}

	fun setListAdapter(listAdapter: ChatFlowAdapter) {
		this.listAdapter = listAdapter
		attachDataListener()
	}

	private fun attachDataListener() {
		repository.attachListener(object : RequestCallback<List<Message>>{
			override fun onSuccess(result: List<Message>) {
				listAdapter.setItems(result)
				updateMessagesStatus(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	private fun updateMessagesStatus(result: List<Message>) {
		result
			.filter {  it.status != Message.STATUS_RECEIVED
							&& it.senderId != userId }
			.forEach {
				repository.setMessageStatusReceived(it.id, null)
			}
	}

	private fun detachDataListener() {
		repository.detachListener()
	}

	fun onAttachButtonCliked() = openAttachMenuSubj.onNext(EmptyObject)

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
		repository.addMessage(Message(senderId = userId, text = text), null)
	}

	fun handleAttachedImage(fileData: FileData, conpressed: Uri) {
		val message = Message(senderId = userId, attachUrl = conpressed.toString(), type = Message.TYPE_IMAGE)
		repository.addMessage(message, object : SimpleRequestCallback<String>() {
			override fun onSuccess(messageId: String) {
				App.backend.getStorageManager().uploadImage(fileData, chatId, object : ProgressRequestCallback<Uri> {
					override fun onProgress(progress: Int) {
						listAdapter.setFileUploadProgress(messageId, progress)
					}
					override fun onSuccess(uri: Uri) {
						repository.updateMessageUri(messageId, uri, object : SimpleRequestCallback<Any>() {})
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
					}
				})
			}
		})
	}

	fun handleAttachedFile(fileData: FileData) {
		val message = Message(userId, fileData)
		repository.addMessage(message, object : SimpleRequestCallback<String>() {
			override fun onSuccess(messageId: String) {
				App.backend.getStorageManager().uploadFile(fileData, chatId, object : ProgressRequestCallback<Uri> {
					override fun onProgress(progress: Int) {
						listAdapter.setFileUploadProgress(messageId, progress)
					}
					override fun onSuccess(result: Uri) {
						repository.updateMessageUri(messageId, result, object : SimpleRequestCallback<Any>() {})
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
					}
				})
			}
		})
	}

	fun handleAudioMessage(file: File) {
		val uri = Uri.fromFile(file)
		val message = Message.createFromAudio(userId, file.name)
		repository.addMessage(message, object : SimpleRequestCallback<String>() {
			override fun onSuccess(messageId: String) {
				App.backend.getStorageManager().uploadAudioMessage(uri, chatId, object : ProgressRequestCallback<Uri> {
					override fun onProgress(progress: Int) {
						listAdapter.setFileUploadProgress(messageId, progress)
					}
					override fun onSuccess(result: Uri) {
						repository.updateMessageUri(messageId, result, object : SimpleRequestCallback<Any>() {})
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
					}
				})
			}
		})
	}

	fun filter(text: String) {
		listAdapter.filter(text)
	}

	fun observeAttachMenuOpen() = openAttachMenuSubj as Observable<Any>

	fun observeAudioRecord() = recordAudioSubj as Observable<Any>
}

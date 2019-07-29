package com.ancientlore.intercom.ui.chat.flow

import android.net.Uri
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.FileData
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatFlowViewModel(private val userId: String,
                        private val chatId: String)
	: BasicViewModel() {

	val textField = ObservableField<String>("")

	private lateinit var listAdapter: ChatFlowAdapter

	private val repository = MessageRepository()

	private val messageText get() = textField.get()!!

	private val openAttachMenuSubj = PublishSubject.create<Any>()

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

	private fun sendMessage(text: String) {
		repository.addMessage(Message(senderId = userId, text = text), null)
	}

	fun handleAttachedImage(fileData: FileData) {
		val message = Message(senderId = userId, attachUrl = fileData.uri.toString(), type = Message.TYPE_IMAGE)
		repository.addMessage(message, object : RequestCallback<String> {
			override fun onSuccess(result: String) {
				val messageId = result
				App.backend.getStorageManager().uploadImage(fileData, chatId, object : RequestCallback<Uri> {
					override fun onSuccess(result: Uri) {
						repository.updateMessageUri(messageId, result, object : RequestCallback<Any> {
							override fun onSuccess(result: Any) {
								toastRequest.onNext(R.string.success) // TODO remove this callback on release
							}
							override fun onFailure(error: Throwable) {
								toastRequest.onNext(R.string.error)
								Utils.logError(error)
							}
						})
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
					}
				})
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun handleAttachedFile(fileData: FileData) {
		val message = Message(userId, fileData)
		repository.addMessage(message, object : RequestCallback<String> {
			override fun onSuccess(result: String) {
				val messageId = result
				App.backend.getStorageManager().uploadFile(fileData, chatId, object : RequestCallback<Uri> {
					override fun onSuccess(result: Uri) {
						repository.updateMessageUri(messageId, result, object : RequestCallback<Any> {
							override fun onSuccess(result: Any) {
								toastRequest.onNext(R.string.success) // TODO remove this callback on release
							}
							override fun onFailure(error: Throwable) {
								toastRequest.onNext(R.string.error)
								Utils.logError(error)
							}
						})
					}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
					}
				})
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun observeAttachMenuOpen() = openAttachMenuSubj as Observable<Any>
}

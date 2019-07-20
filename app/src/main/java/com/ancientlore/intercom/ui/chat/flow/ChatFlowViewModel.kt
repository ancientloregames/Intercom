package com.ancientlore.intercom.ui.chat.flow

import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatFlowViewModel(private val userId: String, chatId: String) : BasicViewModel() {

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
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
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

	fun observeAttachMenuOpen() = openAttachMenuSubj as Observable<Any>
}

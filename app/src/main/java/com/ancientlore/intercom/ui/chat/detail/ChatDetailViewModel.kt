package com.ancientlore.intercom.ui.chat.detail

import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.Utils

class ChatDetailViewModel(private val userId: String, chatId: String) : BasicViewModel() {

	val textField = ObservableField<String>("")

	private lateinit var listAdapter: ChatDetailAdapter

	private val repository = MessageRepository()

	private val messageText get() = textField.get()!!

	init {
		val dataSourceProvider = App.backend.getDataSourceProvider()
		repository.setRemoteSource(dataSourceProvider.getMessageSource(chatId))
	}

	override fun onCleared() {
		super.onCleared()
		detachDataListener()
	}

	fun setListAdapter(listAdapter: ChatDetailAdapter) {
		this.listAdapter = listAdapter
		attachDataListener()
	}

	fun onSendButtonClicked() {
		messageText.takeIf { it.isNotBlank() }
			?.let {
				sendMessage(it)
				textField.set("")
			}
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

	private fun sendMessage(text: String) {
		repository.addMessage(Message(senderId = userId, text = text), null)
	}
}

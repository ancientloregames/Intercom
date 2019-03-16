package com.ancientlore.intercom.ui.chat.detail

import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.BasicViewModel

class ChatDetailViewModel(private val userId: String, chatId: String) : BasicViewModel() {

	val textField = ObservableField<String>("")

	private lateinit var listAdapter: ChatDetailAdapter

	private val repository = MessageRepository()

	private val messageText get() = textField.get()!!

	init {
		val dataSourceProvider = App.backend.getDataSourceProvider()
		repository.setRemoteSource(dataSourceProvider.getMessageSource(chatId))
	}

	fun setListAdapter(listAdapter: ChatDetailAdapter) {
		this.listAdapter = listAdapter
		loadMessages()
	}

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

	private fun loadMessages() {
		repository.getAll(object : SimpleRequestCallback<List<Message>>() {
			override fun onSuccess(result: List<Message>) {
				listAdapter.setItems(result)
			}
		})
	}
}

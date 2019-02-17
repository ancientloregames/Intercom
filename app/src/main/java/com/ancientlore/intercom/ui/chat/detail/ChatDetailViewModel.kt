package com.ancientlore.intercom.ui.chat.detail

import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.BasicViewModel

class ChatDetailViewModel(chatId: String) : BasicViewModel() {

	private lateinit var listAdapter: ChatDetailAdapter

	private val repository = MessageRepository()

	init {
		val dataSourceProvider = App.backend.getDataSourceProvider()
		repository.setRemoteSource(dataSourceProvider.getMessageSource(chatId))
	}

	fun setListAdapter(listAdapter: ChatDetailAdapter) {
		this.listAdapter = listAdapter
		loadMessages()
	}

	private fun loadMessages() {
		repository.getAll(object : SimpleRequestCallback<List<Message>>() {
			override fun onSuccess(result: List<Message>) {
				listAdapter.setItems(result)
			}
		})
	}
}

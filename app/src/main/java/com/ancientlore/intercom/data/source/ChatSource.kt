package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat

interface ChatSource : DataSource<Chat> {

	fun getItem(id: String, callback: RequestCallback<Chat>)

	/**
	 * @param callback onSuccess gets created chat server id as the parameter
	 */
	fun addItem(item: Chat, callback: RequestCallback<String>?)

	fun deleteItem(chatId: String, callback: RequestCallback<Any>? = null)

	fun updateItem(item: Chat, callback: RequestCallback<Any>? = null)

	fun attachListener(callback: RequestCallback<List<Chat>>) : RepositorySubscription
}
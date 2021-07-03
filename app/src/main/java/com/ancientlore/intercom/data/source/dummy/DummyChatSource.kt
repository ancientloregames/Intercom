package com.ancientlore.intercom.data.source.dummy

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.utils.Utils

object DummyChatSource : ChatSource {

	const val TAG = "DummyChatSource"

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		Utils.logError("$TAG.getAll")
	}
	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		Utils.logError("$TAG.getItem")
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {
		Utils.logError("$TAG.addItem")
	}

	override fun deleteItem(chatId: String, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.deleteItem")
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.updateItem")
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>): RepositorySubscription {
		Utils.logError("$TAG.attachListener")

		return object : RepositorySubscription {
			override fun remove() {}
		}
	}
}
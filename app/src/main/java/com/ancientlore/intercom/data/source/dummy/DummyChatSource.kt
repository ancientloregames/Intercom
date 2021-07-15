package com.ancientlore.intercom.data.source.dummy

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.EmptyResultException

object DummyChatSource : ChatSource {

	override fun getSourceId() = ""

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		callback.onFailure(EmptyResultException)
	}
	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItems(items: List<Chat>, callback: RequestCallback<List<String>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {
		callback.onFailure(EmptyResultException)
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {}
		}
	}

	override fun attachListener(id: String, callback: RequestCallback<Chat>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {}
		}
	}
}
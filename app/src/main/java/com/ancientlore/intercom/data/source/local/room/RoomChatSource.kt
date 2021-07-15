package com.ancientlore.intercom.data.source.local.room

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.EmptyResultException

class RoomChatSource(private val userId: String,
                     private val dao: RoomChatDao) : RoomSource(), ChatSource {

	override fun getWorkerThreadName() = "roomChatSource_thread"

	override fun getSourceId() = userId

	override fun getAll(callback: RequestCallback<List<Chat>>) {

		exec {
			val items = dao.getAll(userId)
			if (items.isNotEmpty())
				callback.onSuccess(items)
			else
				callback.onFailure(EmptyResultException)
		}
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {

		exec {
			dao.getById(id)
				?.let { callback.onSuccess(it) }
				?: callback.onFailure(EmptyResultException)
		}
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {

		exec {
			item.userId = userId

			dao.insert(item)
		}
	}

	override fun addItems(items: List<Chat>, callback: RequestCallback<List<String>>) {

		exec {
			for (item in items)
				item.userId = userId

			dao.insert(items)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {

		exec {
			dao.deleteById(id)
		}
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {

		exec {
			if (item.name.isNotEmpty())
				dao.updateName(item.id, item.name)
			if (item.iconUrl.isNotEmpty())
				dao.updateIconUrl(item.id, item.iconUrl)
			callback.onSuccess(EmptyObject)
		}
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>): RepositorySubscription {
		TODO("Not yet implemented")
	}

	override fun attachListener(id: String, callback: RequestCallback<Chat>): RepositorySubscription {
		TODO("Not yet implemented")
	}
}
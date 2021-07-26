package com.ancientlore.intercom.data.source.local.room

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.MessageSource

class RoomMessageSource(private val chatId: String,
                        private val dao: RoomMessageDao) : RoomSource(), MessageSource {

	override fun getWorkerThreadName() = "roomMessageSource_thread"

	override fun getSourceId() = chatId

	override fun getAll(callback: RequestCallback<List<Message>>) {

		exec {
			val items = dao.getAll(chatId)
			if (items.isNotEmpty())
				callback.onSuccess(items)
			else
				callback.onFailure(EmptyResultException)
		}
	}

	override fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>) {

		exec {
			val items = dao.getAllByIds(ids)
			if (items.isNotEmpty())
				callback.onSuccess(items)
			else
				callback.onFailure(EmptyResultException)
		}
	}

	override fun getItem(id: String, callback: RequestCallback<Message>) {

		exec {
			dao.getById(id, chatId)
				?.let { callback.onSuccess(it) }
				?: callback.onFailure(EmptyResultException)
		}
	}

	override fun addItem(item: Message, callback: RequestCallback<String>) {

		exec {
			item.chatId = chatId

			dao.insert(item)
		}
	}

	override fun addItems(items: List<Message>, callback: RequestCallback<List<String>>) {

		exec {
			for (message in items)
				message.chatId = chatId

			dao.insert(items)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {

		exec {
			dao.deleteById(id, chatId)
		}
	}

	override fun updateMessageUri(id: String, uri: Uri, callback: RequestCallback<Any>) {

		exec {
			dao.updateMessageUri(id, uri.toString(), chatId)
		}
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {

		exec {
			dao.setStatusReceived(id, chatId)
		}
	}

	override fun attachListener(callback: RequestCallback<List<Message>>): RepositorySubscription {
		TODO("Not yet implemented")
	}

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {
		TODO("Not yet implemented")
	}
}
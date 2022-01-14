package com.ancientlore.intercom.data.source.local.room

import android.net.Uri
import com.ancientlore.intercom.C
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.ListChanges
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.utils.Utils

class RoomMessageSource(private val chatId: String,
                        private val dao: RoomMessageDao) : RoomSource(), MessageSource {

	private var paginationLimit: Long = C.DEF_MSG_PAGINATION_LIMIT
	private var currentPageOffset: Long = 0

	@Volatile
	private var paginationCompleted: Boolean = false

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

	override fun getItems(ids: List<String>, callback: RequestCallback<List<Message>>) {

		exec {
			val items = dao.getAllByIds(ids)
			if (items.isNotEmpty())
				callback.onSuccess(items)
			else
				callback.onFailure(EmptyResultException)
		}
	}

	override fun getNextPage(callback: RequestCallback<List<Message>>) { // TODO need tests

		if (paginationCompleted) {
			callback.onSuccess(emptyList())
			return
		}

		exec {
			val nextPageMessages = dao.getWithLimit(chatId, paginationLimit, currentPageOffset)
			if (nextPageMessages.isNotEmpty()) {
				currentPageOffset += nextPageMessages.size
				callback.onSuccess(nextPageMessages)
			}
			else {
				paginationCompleted = true
				callback.onSuccess(emptyList())
			}
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

	override fun setPaginationLimit(limit: Long) {
		if (limit > 1)
			this.paginationLimit = limit
		else
			Utils.logError("Pagination limit must be > 1")
	}

	override fun attachChangeListener(callback: RequestCallback<ListChanges<Message>>): RepositorySubscription {
		TODO("Not yet implemented")
	}

	override fun attachListener(callback: RequestCallback<List<Message>>): RepositorySubscription {
		TODO("Not yet implemented")
	}

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {
		TODO("Not yet implemented")
	}
}
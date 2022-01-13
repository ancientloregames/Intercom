package com.ancientlore.intercom.data.source.dummy

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.ListChanges
import com.ancientlore.intercom.data.source.MessageSource

object DummyMessageSource : MessageSource {

	override fun getSourceId() = ""

	override fun getAll(callback: RequestCallback<List<Message>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getItem(id: String, callback: RequestCallback<Message>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: Message, callback: RequestCallback<String>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItems(items: List<Message>, callback: RequestCallback<List<String>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getNextPage(callback: RequestCallback<List<Message>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun setPaginationLimit(limit: Long) {
	}

	override fun attachListener(callback: RequestCallback<List<Message>>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}
}
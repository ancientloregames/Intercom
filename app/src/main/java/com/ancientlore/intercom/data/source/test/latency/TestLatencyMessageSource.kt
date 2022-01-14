package com.ancientlore.intercom.data.source.test.latency

import android.net.Uri
import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.ListChanges
import com.ancientlore.intercom.data.source.test.TestMessageSource

abstract class TestLatencyMessageSource: TestMessageSource(), LatencySource {

	override fun getAll(callback: RequestCallback<List<Message>>) {
		schedule {
			super.getAll(callback)
		}
	}

	override fun getItems(ids: List<String>, callback: RequestCallback<List<Message>>) {
		schedule {
			super.getItems(ids, callback)
		}
	}

	override fun getItem(id: String, callback: RequestCallback<Message>) {
		schedule {
			super.getItem(id, callback)
		}
	}

	override fun getNextPage(callback: RequestCallback<List<Message>>) {
		schedule {
			super.getNextPage(callback)
		}
	}

	override fun addItem(item: Message, callback: RequestCallback<String>) {
		schedule {
			super.addItem(item, callback)
		}
	}

	override fun addItems(items: List<Message>, callback: RequestCallback<List<String>>) {
		schedule {
			super.addItems(items, callback)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		schedule {
			super.deleteItem(id, callback)
		}
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {
		schedule {
			super.updateMessageUri(messageId, uri, callback)
		}
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {
		schedule {
			super.setMessageStatusReceived(id, callback)
		}
	}

	override fun setPaginationLimit(limit: Long) {
		super.setPaginationLimit(limit)
	}

	override fun attachListener(callback: RequestCallback<List<Message>>): RepositorySubscription {
		schedule {
			super.attachListener(callback)
		}

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {
		schedule {
			super.attachListener(id, callback)
		}

		return DummyRepositorySubscription
	}

	override fun attachChangeListener(callback: RequestCallback<ListChanges<Message>>): RepositorySubscription {
		schedule {
			super.attachChangeListener(callback)
		}

		return DummyRepositorySubscription
	}
}
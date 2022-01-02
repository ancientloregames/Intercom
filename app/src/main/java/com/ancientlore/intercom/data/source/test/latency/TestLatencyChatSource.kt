package com.ancientlore.intercom.data.source.test.latency

import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.test.TestChatSource

abstract class TestLatencyChatSource: TestChatSource(), LatencySource {

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		schedule {
			super.getAll(callback)
		}
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		schedule {
			super.getItem(id, callback)
		}
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {
		schedule {
			super.addItem(item, callback)
		}
	}

	override fun addItems(items: List<Chat>, callback: RequestCallback<List<String>>) {
		schedule {
			super.addItems(items, callback)
		}
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {
		schedule {
			super.updateItem(item, callback)
		}
	}

	override fun setMessageRecieved(id: String, callback: RequestCallback<Any>) {
		schedule {
			super.setMessageRecieved(id, callback)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		schedule {
			super.deleteItem(id, callback)
		}
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>): RepositorySubscription {
		schedule {
			super.attachListener(callback)
		}

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<Chat>): RepositorySubscription {
		schedule {
			super.attachListener(id, callback)
		}

		return DummyRepositorySubscription
	}

	override fun getBroadcasts(callback: RequestCallback<List<Chat>>) {
		super.getBroadcasts(callback)
	}
}
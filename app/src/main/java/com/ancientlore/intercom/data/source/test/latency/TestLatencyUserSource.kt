package com.ancientlore.intercom.data.source.test.latency

import android.net.Uri
import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.test.TestUserSource

abstract class TestLatencyUserSource: TestUserSource(), LatencySource {

	override fun getAll(callback: RequestCallback<List<User>>) {
		schedule {
			super.getAll(callback)
		}
	}

	override fun getItem(id: String, callback: RequestCallback<User>) {
		schedule {
			super.getItem(id, callback)
		}
	}

	override fun addItem(item: User, callback: RequestCallback<String>) {
		schedule {
			super.addItem(item, callback)
		}
	}

	override fun addItems(items: List<User>, callback: RequestCallback<List<String>>) {
		schedule {
			super.addItems(items, callback)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		schedule {
			super.deleteItem(id, callback)
		}
	}

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {
		schedule {
			super.updateNotificationToken(token, callback)
		}
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {
		schedule {
			super.updateIcon(uri, callback)
		}
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {
		schedule {
			super.updateName(name, callback)
		}
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {
		schedule {
			super.updateStatus(status, callback)
		}
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {
		schedule {
			super.updateOnlineStatus(online, callback)
		}
	}

	override fun attachListener(callback: RequestCallback<List<User>>): RepositorySubscription {
		schedule {
			super.attachListener(callback)
		}

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<User>): RepositorySubscription {
		schedule {
			super.attachListener(id, callback)
		}

		return DummyRepositorySubscription
	}
}
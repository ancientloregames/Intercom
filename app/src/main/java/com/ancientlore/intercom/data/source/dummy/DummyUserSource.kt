package com.ancientlore.intercom.data.source.dummy

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.UserSource

object DummyUserSource : UserSource {

	override fun getSourceId() = ""

	override fun getAll(callback: RequestCallback<List<User>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getItem(id: String, callback: RequestCallback<User>) {
		callback.onFailure(EmptyResultException)
	}

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: User, callback: RequestCallback<String>) {
		callback.onFailure(EmptyResultException)
	}

	override fun addItems(items: List<User>, callback: RequestCallback<List<String>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {
		callback.onFailure(EmptyResultException)
	}

	override fun attachListener(callback: RequestCallback<List<User>>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}

	override fun attachListener(id: String, callback: RequestCallback<User>): RepositorySubscription {
		callback.onFailure(EmptyResultException)

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}
}
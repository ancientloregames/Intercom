package com.ancientlore.intercom.data.source.local.room

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.UserSource

class RoomUserSource(private val userId: String,
                     private val dao: RoomUserDao) : RoomSource(), UserSource {

	override fun getWorkerThreadName() = "roomUserSource_thread"

	override fun getSourceId() = userId

	override fun getAll(callback: RequestCallback<List<User>>) {

		exec {
			val items = dao.getAll()
			if (items.isNotEmpty())
				callback.onSuccess(items)
			else
				callback.onFailure(EmptyResultException)
		}
	}

	override fun getItem(id: String, callback: RequestCallback<User>) {

		exec {
			dao.getById(id)
				?.let { callback.onSuccess(it) }
				?: callback.onFailure(EmptyResultException)
		}
	}

	override fun addItem(item: User, callback: RequestCallback<String>) {

		exec {
			dao.insert(item)
		}
	}

	override fun addItems(items: List<User>, callback: RequestCallback<List<String>>) {

		exec {
			dao.insert(items)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		TODO("Not yet implemented")
	}

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {

		exec {
			dao.updateNotificationToken(userId, token)
		}
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {

		exec {
			dao.updateIconUrl(userId, uri.toString())
		}
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {

		exec {
			dao.updateName(userId, name)
		}
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {

		exec {
			dao.updateStatus(userId, status)
		}
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {

		exec {
			dao.updateOnlineStatus(userId, online)
		}
	}

	override fun attachListener(callback: RequestCallback<List<User>>): RepositorySubscription {
		TODO("Not yet implemented")
	}

	override fun attachListener(id: String, callback: RequestCallback<User>): RepositorySubscription {
		TODO("Not yet implemented")
	}
}
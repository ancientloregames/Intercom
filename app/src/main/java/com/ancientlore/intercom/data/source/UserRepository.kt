package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.cache.CacheUserSource
import com.ancientlore.intercom.data.source.dummy.DummyUserSource
import com.ancientlore.intercom.utils.Utils

object UserRepository : UserSource {

	private var remoteSource : UserSource = DummyUserSource
	private val cacheSource = CacheUserSource

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {

		remoteSource.updateNotificationToken(token, callback)
		// FIXME to update cache, need userId. Same with other updates
	}

	override fun getAll(callback: RequestCallback<List<User>>) {

		remoteSource.getAll(object : RequestCallback<List<User>> {

			override fun onSuccess(result: List<User>) {
				cacheSource.reset(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				cacheSource.getAll()
					.takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException())
			}
		})
	}

	override fun getItem(phoneNumber: String, callback: RequestCallback<User>) {

		remoteSource.getItem(phoneNumber, object : RequestCallback<User> {

			override fun onSuccess(result: User) {
				cacheSource.setItem(phoneNumber, result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				cacheSource.getItem(phoneNumber)
					?.run { callback.onSuccess(this) }
					?: callback.onFailure(EmptyResultException())
			}
		})
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {

		remoteSource.updateIcon(uri, callback)
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {

		remoteSource.updateName(name, callback)
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {

		remoteSource.updateStatus(status, callback)
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {

		remoteSource.updateOnlineStatus(online, callback)
	}

	override fun attachListener(userId: String, callback: RequestCallback<User>): RepositorySubscription {

		return remoteSource.attachListener(userId, object : RequestCallback<User> {

			override fun onSuccess(result: User) {
				cacheSource.setItem(userId, result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	fun setRemoteSource(source: UserSource) {
		remoteSource = source
	}

	fun hasRemoteSource() = remoteSource !is DummyUserSource
}
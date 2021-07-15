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
	private var localSource : UserSource? = null
	private val cacheSource = CacheUserSource

	override fun getSourceId() = remoteSource.getSourceId()

	fun hasRemoteSource() = remoteSource !is DummyUserSource

	override fun getAll(callback: RequestCallback<List<User>>) {

		remoteSource.getAll(object : RequestCallback<List<User>> {

			override fun onSuccess(result: List<User>) {
				cacheSource.reset(result)
				localSource?.addItems(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getAllFallback(callback)
			}
		})
	}

	override fun getItem(id: String, callback: RequestCallback<User>) {

		remoteSource.getItem(id, object : RequestCallback<User> {

			override fun onSuccess(result: User) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	override fun addItem(item: User, callback: RequestCallback<String>) {

		remoteSource.addItem(item, object : RequestCallback<String> {

			override fun onSuccess(result: String) {
				cacheSource.addItem(item)
				localSource?.addItem(item)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun addItems(items: List<User>, callback: RequestCallback<List<String>>) {

		remoteSource.addItems(items, object : RequestCallback<List<String>> {

			override fun onSuccess(result: List<String>) {
				cacheSource.addItems(items)
				localSource?.addItems(items)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		TODO("Not yet implemented")
	}

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {

		remoteSource.updateNotificationToken(token, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateNotificationToken(getSourceId(), token)
				localSource?.updateNotificationToken(token)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {

		remoteSource.updateIcon(uri, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateIcon(getSourceId(), uri)
				localSource?.updateIcon(uri)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {

		remoteSource.updateName(name, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateName(getSourceId(), name)
				localSource?.updateName(name)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {

		remoteSource.updateStatus(status, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateStatus(getSourceId(), status)
				localSource?.updateStatus(status)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {

		remoteSource.updateOnlineStatus(online, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateOnlineStatus(getSourceId(), online)
				localSource?.updateOnlineStatus(online)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun attachListener(callback: RequestCallback<List<User>>): RepositorySubscription {

		return remoteSource.attachListener(object : RequestCallback<List<User>> {

			override fun onSuccess(result: List<User>) {
				cacheSource.reset(result)
				localSource?.addItems(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getAllFallback(callback)
			}
		})
	}

	override fun attachListener(id: String, callback: RequestCallback<User>): RepositorySubscription {

		return remoteSource.attachListener(id, object : RequestCallback<User> {

			override fun onSuccess(result: User) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	fun setRemoteSource(source: UserSource) {
		remoteSource = source

		cacheSource.clear()
		localSource?.let {
			if (source.getSourceId() != it.getSourceId())
				localSource = null
		}
	}

	fun setLocalSource(source: UserSource) {
		localSource = source

		if (source.getSourceId() != remoteSource.getSourceId()) {
			cacheSource.clear()
			remoteSource = DummyUserSource
		}
	}

	private fun getAllFallback(callback: RequestCallback<List<User>>) {

		cacheSource.getAll(object : RequestCallback<List<User>> {

			override fun onSuccess(result: List<User>) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getAll(object : RequestCallback<List<User>> {

						override fun onSuccess(result: List<User>) {
							cacheSource.reset(result)
							callback.onSuccess(result)
						}
						override fun onFailure(error: Throwable) {
							callback.onFailure(EmptyResultException)
						}
					}) }
			}
		})
	}

	private fun getItemFallback(id: String, callback: RequestCallback<User>) {

		cacheSource.getItem(id, object : RequestCallback<User> {

			override fun onSuccess(result: User) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getItem(id, object : RequestCallback<User> {

						override fun onSuccess(result: User) {
							cacheSource.addItem(result)
							callback.onSuccess(result)
						}
						override fun onFailure(error: Throwable) {
							callback.onFailure(EmptyResultException)
						}
					}) }
			}
		})
	}
}
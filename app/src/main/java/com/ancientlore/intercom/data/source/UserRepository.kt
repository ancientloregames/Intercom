package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.cache.CacheUserSource

object UserRepository : UserSource {

	private var remoteSource : UserSource? = null
	private val cacheSource = CacheUserSource

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>?) {
		remoteSource?.updateNotificationToken(token, callback)
	}

	override fun getAll(callback: RequestCallback<List<User>>) {
		remoteSource
			?.run {
				getAll(object : RequestCallback<List<User>> {
					override fun onSuccess(result: List<User>) {
						cacheSource.reset(result)
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) {
						error.printStackTrace()
						callback.onSuccess(cacheSource.getAll())
					}
				})
			}
			?: callback.onSuccess(cacheSource.getAll())
	}

	override fun getItem(phoneNumber: String, callback: RequestCallback<User>) {
		remoteSource
			?.run {
				getItem(phoneNumber, object : RequestCallback<User> {
					override fun onSuccess(result: User) {
						cacheSource.setItem(phoneNumber)
						callback.onSuccess(result)
					}
					override fun onFailure(error: Throwable) {
						error.printStackTrace()
						cacheSource.getItem(phoneNumber)
							?.run { callback.onSuccess(this) }
							?: callback.onFailure(EmptyResultException())
					}
				})
			}
	}

	fun setRemoteSource(source: UserSource) {
		remoteSource = source
	}

	fun hasRemoteSource() = remoteSource != null
}
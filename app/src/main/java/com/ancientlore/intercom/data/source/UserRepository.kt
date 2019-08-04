package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User

object UserRepository : UserSource {

	private var remoteSource : UserSource? = null

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>?) {
		remoteSource?.updateNotificationToken(token, callback)
	}

	override fun getAll(callback: RequestCallback<List<User>>) {
	}

	fun setRemoteSource(source: UserSource) {
		remoteSource = source
	}

	fun hasRemoteSource() = remoteSource != null
}
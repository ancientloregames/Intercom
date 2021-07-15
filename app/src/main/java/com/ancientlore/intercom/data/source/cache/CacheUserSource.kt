package com.ancientlore.intercom.data.source.cache

import android.net.Uri
import com.ancientlore.intercom.data.model.User

object CacheUserSource : CacheSource<String, User>() {

	fun updateNotificationToken(userId: String, token: String) {

		cache[userId]?.token = token
	}

	fun updateIcon(userId: String, uri: Uri) {

		cache[userId]?.iconUrl = uri.toString()
	}

	fun updateName(userId: String, name: String) {

		cache[userId]?.name = name
	}

	fun updateStatus(userId: String, status: String) {

		cache[userId]?.status = status
	}

	fun updateOnlineStatus(userId: String, online: Boolean) {

		cache[userId]?.online = online
	}
}
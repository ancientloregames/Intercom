package com.ancientlore.intercom.data.source.dummy

import android.net.Uri
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.UserSource
import com.ancientlore.intercom.utils.Utils

object DummyUserSource : UserSource {

	const val TAG = "DummyUserSource"

	override fun getAll(callback: RequestCallback<List<User>>) {
		Utils.logError("$TAG.getAll")
	}

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.updateNotificationToken")
	}

	override fun getItem(phoneNumber: String, callback: RequestCallback<User>) {
		Utils.logError("$TAG.getItem")
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.updateIcon")
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.updateName")
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.updateStatus")
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {
		Utils.logError("$TAG.updateOnlineStatus")
	}

	override fun attachListener(
		userId: String,
		callback: RequestCallback<User>
	): RepositorySubscription {
		Utils.logError("$TAG.attachListener")

		return object : RepositorySubscription {
			override fun remove() {
			}
		}
	}
}
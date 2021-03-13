package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User

interface UserSource : DataSource<User> {
	fun updateNotificationToken(token: String, callback: RequestCallback<Any>?)
	fun getItem(phoneNumber: String, callback: RequestCallback<User>)
	fun updateIcon(uri: Uri, callback: RequestCallback<Any>? = null)
}
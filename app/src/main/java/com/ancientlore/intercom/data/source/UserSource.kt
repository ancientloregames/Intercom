package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.callback.RequestCallbackAny
import com.ancientlore.intercom.data.model.User

interface UserSource : DataSource<String, User> {

	fun updateNotificationToken(token: String, callback: RequestCallback<Any> = RequestCallbackAny)
	fun updateIcon(uri: Uri, callback: RequestCallback<Any> = RequestCallbackAny)
	fun updateName(name: String, callback: RequestCallback<Any> = RequestCallbackAny)
	fun updateStatus(status: String, callback: RequestCallback<Any> = RequestCallbackAny)
	fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any> = RequestCallbackAny)
}
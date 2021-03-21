package com.ancientlore.intercom.data.source.remote.firestore

import android.net.Uri
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.UserSource
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.data.source.remote.firestore.C.USER_FCM_TOKEN
import com.ancientlore.intercom.data.source.remote.firestore.C.USER_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.USER_NAME
import com.google.firebase.firestore.SetOptions

class FirestoreUserSource(private val userId: String)
	: FirestoreSource<User>(), UserSource {

	internal companion object  {
		private const val TAG = "FirestoreUserSource"
	}

	private val user get() = users.document(userId)

	private val users get() = db.collection(USERS)

	override fun getObjectClass() = User::class.java

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>?) {
		user.update(USER_FCM_TOKEN, token)
			.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun getAll(callback: RequestCallback<List<User>>) {
		users.get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot).takeIf { it.isNotEmpty() }
					?.let { callback.onSuccess(it) }
					?: callback.onSuccess(emptyList())
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun getItem(phoneNumber: String, callback: RequestCallback<User>) {
		users.document(phoneNumber).get()
			.addOnSuccessListener { snapshot ->
				deserialize(snapshot)
					?.let { callback.onSuccess(it) }
					?: callback.onFailure(EmptyResultException())
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>?) {
		user
			.set(hashMapOf(USER_ICON_URL to uri.toString()), SetOptions.merge())
			.addOnSuccessListener {
				App.backend.getAuthManager().updateUserIconUri(uri, object : RequestCallback<Any> {
					override fun onSuccess(result: Any) { callback?.onSuccess(result) }
					override fun onFailure(error: Throwable) { callback?.onFailure(error) }
				})
			}
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun updateName(name: String, callback: RequestCallback<Any>?) {
		user
			.set(hashMapOf(USER_NAME to name), SetOptions.merge())
			.addOnSuccessListener {
				App.backend.getAuthManager().updateUserName(name, object : RequestCallback<Any> {
					override fun onSuccess(result: Any) { callback?.onSuccess(result) }
					override fun onFailure(error: Throwable) { callback?.onFailure(error) }
				})
			}
			.addOnFailureListener { callback?.onFailure(it) }
	}
}
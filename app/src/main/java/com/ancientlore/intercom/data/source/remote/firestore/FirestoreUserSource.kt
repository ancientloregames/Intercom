package com.ancientlore.intercom.data.source.remote.firestore

import android.net.Uri
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.UserSource
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_FCM_TOKEN
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_SEEN
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ONLINE
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_STATUS
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

open class FirestoreUserSource(protected val userId: String)
	: FirestoreSource<User>(), UserSource {

	internal companion object  {
		private const val TAG = "FirestoreUserSource"
	}

	protected val user get() = users.document(userId)

	protected val users get() = db.collection(USERS)

	override fun getObjectClass() = User::class.java

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>?) {
		user.update(FIELD_FCM_TOKEN, token)
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
			.set(hashMapOf(FIELD_ICON_URL to uri.toString()), SetOptions.merge())
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
			.set(hashMapOf(FIELD_NAME to name), SetOptions.merge())
			.addOnSuccessListener {
				App.backend.getAuthManager().updateUserName(name, object : RequestCallback<Any> {
					override fun onSuccess(result: Any) { callback?.onSuccess(result) }
					override fun onFailure(error: Throwable) { callback?.onFailure(error) }
				})
			}
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>?) {
		user
			.set(hashMapOf(FIELD_STATUS to status), SetOptions.merge())
			.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>?) {
		user
			.set(hashMapOf(
				FIELD_LAST_SEEN to FieldValue.serverTimestamp(),
				FIELD_ONLINE to online), SetOptions.merge())
			.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
			.addOnFailureListener { callback?.onFailure(it) ?: Utils.logError(it) }
	}

}
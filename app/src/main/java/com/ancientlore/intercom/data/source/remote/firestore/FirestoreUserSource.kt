package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.UserSource
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS

class FirestoreUserSource(private val userId: String)
	: FirestoreSource<User>(), UserSource {

	internal companion object  {
		private const val TAG = "FirestoreUserSource"
	}

	private val user get() = users.document(userId)

	private val users get() = db.collection(USERS)

	override fun getObjectClass() = User::class.java

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>?) {
		user.update("token", token)
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
}
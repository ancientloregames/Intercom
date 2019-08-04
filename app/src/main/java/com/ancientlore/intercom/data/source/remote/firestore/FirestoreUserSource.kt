package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.UserSource

class FirestoreUserSource(private val userId: String)
	: FirestoreSource<User>(), UserSource {

	internal companion object  {
		private const val TAG = "FirestoreUserSource"

		private const val USERS = "users"
	}

	private val user get() = db.collection(USERS).document(userId)


	override fun getObjectClass() = User::class.java

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>?) {
		user.update("token", token)
			.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun getAll(callback: RequestCallback<List<User>>) {
	}
}
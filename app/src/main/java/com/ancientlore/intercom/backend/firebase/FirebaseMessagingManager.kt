package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.MessagingManager
import com.ancientlore.intercom.backend.RequestCallback
import com.google.firebase.FirebaseException
import com.google.firebase.iid.FirebaseInstanceId

object FirebaseMessagingManager : MessagingManager {

	override fun getToken(callback: RequestCallback<String>) {
		FirebaseInstanceId.getInstance().instanceId
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					val token = task.result?.token
					if (token != null)
						callback.onSuccess(token)
					else callback.onFailure(FirebaseException("Messaging server returned success, but no token"))
				} else task.exception?.let {
					callback.onFailure(it)
				}
			}
	}

}
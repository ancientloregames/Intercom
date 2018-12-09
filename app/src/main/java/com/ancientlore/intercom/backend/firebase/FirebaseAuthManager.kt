package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager : AuthManager() {

	private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

	override fun signup(params: EmailAuthParams, callback: RequestCallback<User>) {
		auth.createUserWithEmailAndPassword(params.email, params.pass)
			.addOnSuccessListener { result -> callback.onSuccess(User(result.user.uid)) }
			.addOnFailureListener { error -> callback.onFailure(error) }
	}

	override fun login(params: EmailAuthParams, callback: RequestCallback<User>) {
		auth.signInWithEmailAndPassword(params.email, params.pass)
			.addOnSuccessListener { result -> callback.onSuccess(User(result.user.uid)) }
			.addOnFailureListener { error -> callback.onFailure(error) }
	}
}
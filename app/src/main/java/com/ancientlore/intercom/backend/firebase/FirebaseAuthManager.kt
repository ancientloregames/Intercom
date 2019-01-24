package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object FirebaseAuthManager : AuthManager() {

	private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

	override fun signupViaEmail(params: EmailAuthParams, callback: RequestCallback<User>) {
		auth.createUserWithEmailAndPassword(params.email, params.pass)
			.addOnSuccessListener { result -> callback.onSuccess(User(result.user.uid)) }
			.addOnFailureListener { error -> callback.onFailure(error) }
	}

	override fun loginViaEmail(params: EmailAuthParams, callback: RequestCallback<User>) {
		auth.signInWithEmailAndPassword(params.email, params.pass)
			.addOnSuccessListener { result -> callback.onSuccess(User(result.user.uid)) }
			.addOnFailureListener { error -> callback.onFailure(error) }
	}

	override fun getCurrentUser() = auth.currentUser?.let { User(it.uid) }
}
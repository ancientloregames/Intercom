package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object FirebaseAuthManager : AuthManager() {

	private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

	private val authExecutor: Executor by lazy { Executors.newSingleThreadExecutor() }

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

	override fun loginViaPhone(params: PhoneAuthParams, callback: RequestCallback<User>) {
		PhoneAuthProvider.getInstance(auth).verifyPhoneNumber(params.phone,
			60, TimeUnit.SECONDS,
			authExecutor,
			object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
				override fun onVerificationCompleted(creds: PhoneAuthCredential) =
					onPhoneAuthCredential(creds, callback)
				override fun onVerificationFailed(exception: FirebaseException) =
					callback.onFailure(exception)
			}
		)
	}

	override fun getCurrentUser() = auth.currentUser?.let { User(it.uid) }

	private fun onPhoneAuthCredential(credential: PhoneAuthCredential, callback: RequestCallback<User>) {
		auth.signInWithCredential(credential)
			.addOnCompleteListener(authExecutor, OnCompleteListener { task ->
				when {
					task.isSuccessful -> task.result?.user?.let {
						callback.onSuccess(User(it.uid))
					} ?: callback.onFailure(AuthException("Server doesn't return the user credentials"))
					task.exception != null -> callback.onFailure(task.exception!!)
					else -> callback.onFailure(AuthException())
				}
			})
	}
}
package com.ancientlore.intercom.backend.firebase

import android.net.Uri
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.*
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object FirebaseAuthManager : AuthManager {

	private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

	private val authExecutor: Executor by lazy { Executors.newSingleThreadExecutor() }

	private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

	override fun isLoggedIn() = auth.currentUser != null

	override fun logout() = auth.signOut()

	override fun getCurrentUserId(): String {
		return auth.currentUser?.phoneNumber ?: ""
	}

	override fun isNeedPhoneCheck() = false

	override fun getCurrentUser() : User {
		return auth.currentUser
			?.run { toAppUser(this) }
			?: User(dummy = true)
	}

	override fun updateUserIconUri(uri: Uri, callback: RequestCallback<Any>?) {
		auth.currentUser
			?.run {
				updateProfile(
					UserProfileChangeRequest.Builder()
						.setPhotoUri(uri)
						.build())
					.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
					.addOnFailureListener { callback?.onFailure(it) }
			}
			?: Utils.logError("FirebaseAuthManager.updateUserIconUri(): No logged user to update")
	}

	override fun updateUserName(name: String, callback: RequestCallback<Any>?) {
		auth.currentUser
			?.run {
				updateProfile(
					UserProfileChangeRequest.Builder()
						.setDisplayName(name)
						.build())
					.addOnSuccessListener { callback?.onSuccess(EmptyObject) }
					.addOnFailureListener { callback?.onFailure(it) }
			}
			?: Utils.logError("FirebaseAuthManager.updateUserIconUri(): No logged user to update")
	}

	override fun signupViaEmail(params: EmailAuthParams, callback: RequestCallback<User>) {
		auth.createUserWithEmailAndPassword(params.email, params.pass)
			.addOnSuccessListener { result -> callback.onSuccess(toAppUser(result.user)) }
			.addOnFailureListener { error -> callback.onFailure(error) }
	}

	override fun loginViaEmail(params: EmailAuthParams, callback: RequestCallback<User>) {
		auth.signInWithEmailAndPassword(params.email, params.pass)
			.addOnSuccessListener { result -> callback.onSuccess(toAppUser(result.user)) }
			.addOnFailureListener { error -> callback.onFailure(error) }
	}

	override fun loginViaPhone(params: PhoneAuthParams, callback: AuthCallback) {
		PhoneAuthProvider.getInstance(auth).verifyPhoneNumber(params.phone,
			60, TimeUnit.SECONDS,
			authExecutor,
			object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
				override fun onCodeSent(id: String?, token: PhoneAuthProvider.ForceResendingToken) {
					id?.let {
						resendToken = token
						if (params.phone.startsWith("+123456789")) // FIXME Test auth, remove on release
							verifySmsCode("123456", it, callback)
						else callback.onVerification(it)
					}
				}
				override fun onVerificationCompleted(creds: PhoneAuthCredential) =
					onPhoneAuthCredential(creds, callback)
				override fun onVerificationFailed(exception: FirebaseException) =
					callback.onFailure(exception)
			}
		, resendToken)
	}

	override fun verifySmsCode(smsCode: String, validationId: String, callback: RequestCallback<User>) {
		val credential = PhoneAuthProvider.getCredential(validationId, smsCode)
		onPhoneAuthCredential(credential, callback)
	}

	private fun onPhoneAuthCredential(credential: PhoneAuthCredential, callback: RequestCallback<User>) {
		auth.signInWithCredential(credential)
			.addOnCompleteListener(authExecutor) { task ->
				when {
					task.isSuccessful -> task.result?.user
						?.run { callback.onSuccess(toAppUser(this)) }
						?: callback.onFailure(AuthException("Server didn't return the user credentials"))

					task.exception != null -> callback.onFailure(task.exception!!)

					else -> callback.onFailure(AuthException())
				}
			}
	}

	private fun toAppUser(user: FirebaseUser) : User {
		return User(user.displayName ?: "",
			user.phoneNumber ?: "",
			user.email ?: "",
			user.photoUrl?.toString() ?: "")
	}
}
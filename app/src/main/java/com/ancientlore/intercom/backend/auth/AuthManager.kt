package com.ancientlore.intercom.backend.auth

import android.net.Uri
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User

interface AuthManager {
	fun signupViaEmail(params: EmailAuthParams, callback: RequestCallback<User>)
	fun loginViaEmail(params: EmailAuthParams, callback: RequestCallback<User>)

	fun loginViaPhone(params: PhoneAuthParams, callback: RequestCallback<User>)
	fun verifySmsCode(smsCode: String, callback: RequestCallback<User>)
	fun isNeedPhoneCheck() : Boolean

	fun isLoggedIn() : Boolean

	fun logout()

	fun getCurrentUserId() : String
	fun getCurrentUser() : User
	fun updateUserIconUri(uri: Uri, callback: RequestCallback<Any>? = null)
	fun updateUserName(name: String, callback: RequestCallback<Any>? = null)
}
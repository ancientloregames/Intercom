package com.ancientlore.intercom.backend.auth

import com.ancientlore.intercom.backend.RequestCallback

abstract class AuthManager {
	abstract fun signupViaEmail(params: EmailAuthParams, callback: RequestCallback<User>)
	abstract fun loginViaEmail(params: EmailAuthParams, callback: RequestCallback<User>)

	abstract fun loginViaPhone(params: PhoneAuthParams, callback: RequestCallback<User>)
	abstract fun verifySmsCode(smsCode: String, callback: RequestCallback<User>)
	abstract fun isNeedPhoneCheck() : Boolean

	abstract fun getCurrentUser() : User?
}
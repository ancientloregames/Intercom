package com.ancientlore.intercom.backend.auth

import com.ancientlore.intercom.backend.RequestCallback

abstract class AuthManager {
	abstract fun signupViaEmail(params: EmailAuthParams, callback: RequestCallback<User>)
	abstract fun loginViaEmail(params: EmailAuthParams, callback: RequestCallback<User>)

	abstract fun getCurrentUser() : User?
}
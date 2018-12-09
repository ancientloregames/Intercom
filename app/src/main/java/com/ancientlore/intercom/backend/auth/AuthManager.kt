package com.ancientlore.intercom.backend.auth

import com.ancientlore.intercom.backend.RequestCallback

abstract class AuthManager {
	abstract fun signup(params: EmailAuthParams, callback: RequestCallback<User>)
	abstract fun login(params: EmailAuthParams, callback: RequestCallback<User>)

	data class User(val id: String)
}
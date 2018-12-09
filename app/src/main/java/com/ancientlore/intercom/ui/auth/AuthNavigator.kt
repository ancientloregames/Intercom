package com.ancientlore.intercom.ui.auth

import com.ancientlore.intercom.backend.auth.AuthManager


interface AuthNavigator {
	fun openLoginForm()
	fun openSignupForm()
	fun onSuccessfullAuth(user: AuthManager.User)
}
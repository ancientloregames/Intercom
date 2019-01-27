package com.ancientlore.intercom.ui.auth

import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.backend.auth.User


interface AuthNavigator {
	fun openLoginForm()
	fun openSignupForm()
	fun openPhoneAuthForm()
	fun openPhoneCheckForm(params: PhoneAuthParams)
	fun openContactList()
	fun onSuccessfullAuth(user: User)
}
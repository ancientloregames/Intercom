package com.ancientlore.intercom.ui.auth

import androidx.fragment.app.Fragment
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.backend.auth.User


interface AuthNavigator {
	fun openLoginForm()
	fun openSignupForm()
	fun openPhoneAuthForm()
	fun openPhoneCheckForm(params: PhoneAuthParams)
	fun openContactList()
	fun openChatFlow(chatId: String)
	fun onSuccessfullAuth(user: User)
	fun closeFragment(fragment: Fragment)
}
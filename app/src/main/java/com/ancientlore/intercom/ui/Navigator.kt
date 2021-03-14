package com.ancientlore.intercom.ui

import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.utils.Runnable1


interface Navigator {
	fun openLoginForm()
	fun openSignupForm()
	fun openPhoneAuthForm()
	fun openPhoneCheckForm(params: PhoneAuthParams)
	fun openContactList()
	fun openChatFlow(params: ChatFlowFragment.Params)
	fun openSettings()

	fun onSuccessfullAuth(user: User)
	fun closeFragment(fragment: Fragment)

	fun createToolbarMenu(toolbar: Toolbar, callback: Runnable1<Menu>? = null)
}
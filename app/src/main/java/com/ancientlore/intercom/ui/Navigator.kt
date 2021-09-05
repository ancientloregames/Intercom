package com.ancientlore.intercom.ui

import android.net.Uri
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.model.call.Offer
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.ui.contact.detail.ContactDetailParams
import com.ancientlore.intercom.utils.Runnable1


interface Navigator {
	fun openLoginForm()
	fun openSignupForm()
	fun openPhoneAuthForm()
	fun openPhoneCheckForm(params: PhoneAuthParams)
	fun openContactList()
	fun openChatCreation()
	fun openChatFlow(params: ChatFlowParams)
	fun openSettings()
	fun openChatCreationGroup()
	fun openChatCreationDesc(contacts: List<Contact>)
	fun openChatDetail(params: ChatFlowParams)
	fun openContactDetail(params: ContactDetailParams)

	fun openAudioCallOffer(params: CallViewModel.Params)
	fun openVideoCallOffer(params: CallViewModel.Params)
	fun openCallAnswer(offer: Offer)

	fun openImageViewer(uri: Uri)

	fun onSuccessfullAuth(user: User)
	fun closeFragment(fragment: Fragment)

	fun createToolbarMenu(toolbar: Toolbar, callback: Runnable1<Menu>? = null)
}
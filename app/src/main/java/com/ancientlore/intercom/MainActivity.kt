package com.ancientlore.intercom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.BackendManager
import com.ancientlore.intercom.backend.firebase.FirebaseFactory
import com.ancientlore.intercom.ui.auth.AuthNavigator
import com.ancientlore.intercom.ui.auth.login.LoginFragment
import com.ancientlore.intercom.ui.auth.signup.SignupFragment
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), AuthNavigator, BackendManager {

	private val user get() = FirebaseAuth.getInstance().currentUser

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

		if (savedInstanceState == null)
			onFirstStart()
	}

	override fun getBackend() = FirebaseFactory

	private fun onFirstStart() {
		user?.let { openChatList() }
			?: openLoginForm()
	}

	private fun openChatList() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, ChatListFragment.newInstance())
			.commitNow()
	}

	override fun openLoginForm() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, LoginFragment.newInstance())
			.commitNow()
	}

	override fun openSignupForm() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, SignupFragment.newInstance())
			.commitNow()
	}

	override fun openContactList() {
		supportFragmentManager.beginTransaction()
			.add(R.id.container, ContactListFragment.newInstance())
			.commitNow()
	}

	override fun onSuccessfullAuth(user: AuthManager.User) {
		openChatList()
	}
}

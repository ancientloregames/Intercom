package com.ancientlore.intercom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.BackendManager
import com.ancientlore.intercom.backend.firebase.FirebaseFactory
import com.ancientlore.intercom.ui.auth.AuthNavigator
import com.ancientlore.intercom.ui.chatlist.ChatListFragment
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
	}

	override fun openSignupForm() {
	}

	override fun onSuccessfullAuth(user: AuthManager.User) {
		openChatList()
	}
}

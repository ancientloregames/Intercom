package com.ancientlore.intercom

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.BackendManager
import com.ancientlore.intercom.backend.firebase.FirebaseFactory
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.auth.AuthNavigator
import com.ancientlore.intercom.ui.auth.login.LoginFragment
import com.ancientlore.intercom.ui.auth.signup.SignupFragment
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.ancientlore.intercom.utils.PermissionManager
import com.ancientlore.intercom.utils.Runnable1

class MainActivity : AppCompatActivity(), AuthNavigator, BackendManager, PermissionManager {

	companion object {
		private const val PERM_CONTACTS = 101
	}

	private val user get() = getBackend().getAuthManager().getCurrentUser()

	private var permRequestCallback: Runnable1<Boolean>? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

		if (savedInstanceState == null)
			onFirstStart()
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		val result = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
		permRequestCallback?.run(result)
	}

	override fun getBackend() = FirebaseFactory

	private fun onFirstStart() {
		user?.let { onSuccessfullAuth(it) }
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
		initRepositories(user.id)
		openChatList()
	}

	override fun requestContacts(onResult: Runnable1<Boolean>) {
		permRequestCallback = onResult
		ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERM_CONTACTS)
	}

	private fun initRepositories(userId: String) {
		val dataSourceProvider = getBackend().getDataSourceProvider(userId)
		ChatRepository.setRemoteSource(dataSourceProvider.getChatSource())
		MessageRepository.setRemoteSource(dataSourceProvider.getMessageSource())
	}
}

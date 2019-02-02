package com.ancientlore.intercom

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.ui.auth.AuthNavigator
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginFragment
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckFragment
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.ancientlore.intercom.utils.PermissionManager
import com.ancientlore.intercom.utils.Runnable1

class MainActivity : AppCompatActivity(), AuthNavigator, PermissionManager {

	companion object {
		private const val PERM_CONTACTS = 101
	}

	private val user get() = App.backend.getAuthManager().getCurrentUser()

	private var permRequestCallback: Runnable1<Boolean>? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

		if (savedInstanceState == null)
			onFirstStart()
	}

	private fun onFirstStart() {
		user?.let { onSuccessfullAuth(it) }
			?: openPhoneAuthForm()
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		val result = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
		permRequestCallback?.run(result)
	}

	private fun openChatList() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, ChatListFragment.newInstance())
			.commitNow()
	}

	override fun openContactList() {
		supportFragmentManager.beginTransaction()
			.add(R.id.container, ContactListFragment.newInstance())
			.commitNow()
	}

	override fun openLoginForm() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, EmailLoginFragment.newInstance())
			.commitNow()
	}

	override fun openSignupForm() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, EmailSignupFragment.newInstance())
			.commitNow()
	}

	override fun openPhoneAuthForm() {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, PhoneLoginFragment.newInstance())
			.commitNow()
	}

	override fun openPhoneCheckForm(params: PhoneAuthParams) {
		supportFragmentManager.beginTransaction()
			.replace(R.id.container, PhoneCheckFragment.newInstance(params))
			.commitNow()
	}

	override fun onSuccessfullAuth(user: User) {
		initRepositories(user.id)
		openChatList()
	}

	override fun requestContacts(onResult: Runnable1<Boolean>) {
		permRequestCallback = onResult
		ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERM_CONTACTS)
	}

	private fun initRepositories(userId: String) {
		val dataSourceProvider = App.backend.getDataSourceProvider(userId)
		ChatRepository.setRemoteSource(dataSourceProvider.getChatSource())
		MessageRepository.setRemoteSource(dataSourceProvider.getMessageSource())
	}
}

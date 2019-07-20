package com.ancientlore.intercom

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.auth.AuthNavigator
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginFragment
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.ancientlore.intercom.utils.*
import com.ancientlore.intercom.utils.extensions.checkPermission
import com.ancientlore.intercom.utils.extensions.getContacts
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), AuthNavigator, PermissionManager {

	companion object {
		private const val PERM_CONTACTS = 101
		private const val PERM_READ_STORAGE = 102
	}

	interface BackButtonHandler {
		fun onBackPressed(): Boolean
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

	override fun onBackPressed() {
		for (fragment in supportFragmentManager.fragments.reversed())
			if(fragment is BackButtonHandler && fragment.onBackPressed())
				return

		super.onBackPressed()
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

	override fun openChatFlow(params: ChatFlowFragment.Params) {
		supportFragmentManager.beginTransaction()
			.add(R.id.container, ChatFlowFragment.newInstance(params))
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

	override fun closeFragment(fragment: Fragment) {
		supportFragmentManager.beginTransaction()
			.remove(fragment)
			.commitNow()
	}

	override fun onSuccessfullAuth(user: User) {
		initRepositories(user.id)
		trySyncContacts()
		openChatList()
	}

	override fun requestContacts(onResult: Runnable1<Boolean>) {
		permRequestCallback = onResult
		ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERM_CONTACTS)
	}

	override fun requestPermissionReadStorage(onResult: Runnable1<Boolean>) {
		if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
			onResult.run(true)
		else {
			permRequestCallback = onResult
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERM_READ_STORAGE)
		}
	}

	private fun initRepositories(userId: String) {
		val dataSourceProvider = App.backend.getDataSourceProvider()
		ChatRepository.setRemoteSource(dataSourceProvider.getChatSource(userId))
		ContactRepository.setRemoteSource(dataSourceProvider.getContactSource(userId))
	}

	private fun trySyncContacts() {
		if (isContactsSynced().not()) {
			if (checkPermission(Manifest.permission.READ_CONTACTS))
				syncContacts()
			else requestContacts(Runnable1 { granted ->
				if (granted) syncContacts()
			})
		}
	}

	@RequiresPermission(Manifest.permission.READ_CONTACTS)
	private fun syncContacts() {
		Executors.newSingleThreadExecutor().submit {
			contentResolver.getContacts()
				.takeIf { it.isNotEmpty() }
				?.let { contacts -> ContactRepository.addAll(contacts, object : RequestCallback<Any> {
					override fun onSuccess(result: Any) {}
					override fun onFailure(error: Throwable) {
						Utils.logError(error)
						showToast(R.string.alert_error_sync_contacts)
					}
				}) }

			runOnUiThread {
				getPreferences(MODE_PRIVATE).edit().putBoolean(C.PREF_CONTACTS_SYNCED, true).apply()
			}
		}
	}

	private fun isContactsSynced() = getPreferences(Context.MODE_PRIVATE).getBoolean(C.PREF_CONTACTS_SYNCED, false)

	private fun showToast(@StringRes textResId: Int) {
		runOnUiThread {
			Toast.makeText(this, textResId, Toast.LENGTH_LONG).show()
		}
	}
}

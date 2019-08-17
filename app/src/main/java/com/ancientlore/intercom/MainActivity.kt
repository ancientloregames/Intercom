package com.ancientlore.intercom

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.data.source.UserRepository
import com.ancientlore.intercom.ui.Navigator
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginFragment
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.ancientlore.intercom.utils.*
import com.ancientlore.intercom.utils.NotificationManager.Companion.ACTION_OPEN_FROM_PUSH
import com.ancientlore.intercom.utils.NotificationManager.Companion.EXTRA_CHAT_ID
import com.ancientlore.intercom.utils.NotificationManager.Companion.EXTRA_CHAT_TITLE
import com.ancientlore.intercom.utils.extensions.checkPermission
import com.ancientlore.intercom.utils.extensions.createChannel
import com.ancientlore.intercom.utils.extensions.getContacts
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Navigator, PermissionManager {

	companion object {
		private const val PERM_READ_CONTACTS = 101
		private const val PERM_READ_STORAGE = 102
		private const val PERM_WRITE_STORAGE = 103
	}

	interface BackButtonHandler {
		fun onBackPressed(): Boolean
	}

	private val user get() = App.backend.getAuthManager().getCurrentUser()

	private var permRequestCallback: Runnable1<Boolean>? = null

	private var toolbarMenuCallback: Runnable1<Menu>? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			createNotificationChannels()


		if (savedInstanceState == null)
			onFirstStart()
	}

	private fun onFirstStart() {
		user?.let { onSuccessfullAuth(it) }
			?: openPhoneAuthForm()
	}

	override fun onNewIntent(intent: Intent?) {
		if (intent == null || !handleIntent(intent)) {
			super.onNewIntent(intent)
		}
	}

	private fun handleIntent(intent: Intent) : Boolean {
		return when (intent.action) {
			ACTION_OPEN_FROM_PUSH -> {
				intent.extras!!.run {
					val chatId = getString(EXTRA_CHAT_ID)
					val chatTitle = getString(EXTRA_CHAT_TITLE)
					if (chatId != null && chatTitle != null)
						openChatFlow(ChatFlowFragment.Params(chatId, chatTitle))
				}
				true
			}
			else -> false
		}
	}

	override fun onBackPressed() {
		for (fragment in supportFragmentManager.fragments.reversed())
			if(fragment is BackButtonHandler && fragment.onBackPressed())
				return

		super.onBackPressed()
	}

	override fun createToolbarMenu(toolbar: Toolbar, callback: Runnable1<Menu>) {
		toolbarMenuCallback = callback
		setSupportActionBar(toolbar)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		toolbarMenuCallback?.run(menu)

		return true
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		val result = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
		permRequestCallback?.run(result)
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun createNotificationChannels() {
		getSystemService(NotificationManager::class.java)
			?.let {
				it.createChannel(getString(R.string.chat_notification_channel_id),
					getString(R.string.chat_notification_channel_name),
					NotificationManager.IMPORTANCE_DEFAULT)
				it.createChannel(getString(R.string.basic_notification_channel_id),
					getString(R.string.default_notification_channel_name),
					NotificationManager.IMPORTANCE_DEFAULT)
			}
	}

	private fun openChatList() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.container, ChatListFragment.newInstance())
				.commitNow()
		}
	}

	override fun openContactList() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
				.add(R.id.container, ContactListFragment.newInstance())
				.commitNow()
		}
	}

	override fun openChatFlow(params: ChatFlowFragment.Params) {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
				.add(R.id.container, ChatFlowFragment.newInstance(params))
				.commitNowAllowingStateLoss()
		}
	}

	override fun openLoginForm() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.container, EmailLoginFragment.newInstance())
				.commitNow()
		}
	}

	override fun openSignupForm() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.container, EmailSignupFragment.newInstance())
				.commitNow()
		}
	}

	override fun openPhoneAuthForm() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.container, PhoneLoginFragment.newInstance())
				.commitNow()
		}
	}

	override fun openPhoneCheckForm(params: PhoneAuthParams) {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.container, PhoneCheckFragment.newInstance(params))
				.commitNow()
		}
	}

	override fun closeFragment(fragment: Fragment) {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.remove(fragment)
				.commitNow()
		}
	}

	override fun onSuccessfullAuth(user: User) {
		initRepositories(user.id)
		updateNotificationToken()
		trySyncContacts()
		openChatList()
		handleIntent(intent)
	}

	private fun updateNotificationToken() {
		App.backend.getMessagingManager().getToken(object : SimpleRequestCallback<String>() {
			override fun onSuccess(token: String) {
				UserRepository.updateNotificationToken(token, object : SimpleRequestCallback<Any>() {})
			}
		})
	}

	override fun requestPermissionReadContacts(onResult: Runnable1<Boolean>) {
		if (checkPermission(Manifest.permission.READ_CONTACTS))
			onResult.run(true)
		else {
			permRequestCallback = onResult
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERM_READ_CONTACTS)
		}
	}

	override fun requestPermissionReadStorage(onResult: Runnable1<Boolean>) {
		if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
			onResult.run(true)
		else {
			permRequestCallback = onResult
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERM_READ_STORAGE)
		}
	}

	override fun requestPermissionWriteStorage(onResult: Runnable1<Boolean>) {
		if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
			onResult.run(true)
		else {
			permRequestCallback = onResult
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_WRITE_STORAGE)
		}
	}

	private fun initRepositories(userId: String) {
		val dataSourceProvider = App.backend.getDataSourceProvider()
		UserRepository.setRemoteSource(dataSourceProvider.getUserSource(userId))
		ChatRepository.setRemoteSource(dataSourceProvider.getChatSource(userId))
		ContactRepository.setRemoteSource(dataSourceProvider.getContactSource(userId))
	}

	private fun trySyncContacts() {
		if (isContactsSynced().not()) {
			if (checkPermission(Manifest.permission.READ_CONTACTS))
				syncContacts()
			else requestPermissionReadContacts(Runnable1 { granted ->
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

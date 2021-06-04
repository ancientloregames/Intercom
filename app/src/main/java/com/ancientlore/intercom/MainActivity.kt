package com.ancientlore.intercom

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.data.source.UserRepository
import com.ancientlore.intercom.manager.DeviceContactsManager
import com.ancientlore.intercom.ui.Navigator
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginFragment
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckFragment
import com.ancientlore.intercom.ui.chat.creation.ChatCreationFragment
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescFragment
import com.ancientlore.intercom.ui.chat.creation.group.ChatCreationGroupFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.ancientlore.intercom.ui.settings.SettingsFragment
import com.ancientlore.intercom.utils.*
import com.ancientlore.intercom.utils.NotificationManager.Companion.ACTION_OPEN_FROM_PUSH
import com.ancientlore.intercom.utils.NotificationManager.Companion.EXTRA_CHAT_ICON
import com.ancientlore.intercom.utils.NotificationManager.Companion.EXTRA_CHAT_ID
import com.ancientlore.intercom.utils.NotificationManager.Companion.EXTRA_CHAT_TITLE
import com.ancientlore.intercom.utils.NotificationManager.Companion.EXTRA_CHAT_TYPE
import com.ancientlore.intercom.utils.extensions.checkPermission
import com.ancientlore.intercom.utils.extensions.createChannel
import com.ancientlore.intercom.utils.extensions.hideKeyboard
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(),
	Navigator,
	PermissionManager,
	DeviceContactsManager.UpdateListener {

	companion object {
		private const val PERM_READ_CONTACTS = 101
		private const val PERM_READ_STORAGE = 102
		private const val PERM_WRITE_STORAGE = 103
		private const val PERM_AUDIO_MESSAGES = 104

		var isInBackground = false
			private set
	}

	interface BackButtonHandler {
		fun onBackPressed(): Boolean
	}

	private val userContactExecutor = Executors.newSingleThreadExecutor{
			run: Runnable? -> Thread(run, "exec_contactUpdate")
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

	override fun onResume() {
		isInBackground = false

		super.onResume()
	}

	override fun onPause() {
		isInBackground = true

		super.onPause()
	}

	private fun onFirstStart() {
		user.takeIf { it.dummy.not() }
			?.let { onSuccessfullAuth(it) }
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
					val chatType = getInt(EXTRA_CHAT_TYPE)
					val chatTitle = getString(EXTRA_CHAT_TITLE)
					val chatIconUrl = getString(EXTRA_CHAT_ICON)
					if (chatId != null && chatTitle != null && chatIconUrl != null)
						openChatFlow(ChatFlowParams(
							userId = App.backend.getAuthManager().getCurrentUser().id,
							chatId = chatId,
							chatType = chatType,
							title = chatTitle,
							iconUri = Uri.parse(chatIconUrl)))
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

	override fun createToolbarMenu(toolbar: Toolbar, callback: Runnable1<Menu>?) {
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
				.replace(R.id.mainContainer, ChatListFragment.newInstance())
				.commitNow()
		}
	}

	override fun openContactList() {
		tryObserveDeviceContacts { success ->
			if (success) {
				runOnUiThread {
					supportFragmentManager.beginTransaction()
						.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
						.replace(R.id.modalContainer, ContactListFragment.newInstance())
						.commitNow()
				}
			} else {
				//TODO show notification that permission is required
			}
		}
	}

	override fun openChatCreation() {
		tryObserveDeviceContacts { success ->
			if (success) {
				runOnUiThread {
					supportFragmentManager.beginTransaction()
						.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
						.replace(R.id.modalContainer, ChatCreationFragment.newInstance())
						.commitNow()
				}
			} else {
				//TODO show notification that permission is required
			}
		}
	}

	override fun openChatFlow(params: ChatFlowParams) {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
				.replace(R.id.modalContainer, ChatFlowFragment.newInstance(params))
				.commitNowAllowingStateLoss()
		}
	}

	override fun openLoginForm() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.mainContainer, EmailLoginFragment.newInstance())
				.commitNow()
		}
	}

	override fun openSignupForm() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.mainContainer, EmailSignupFragment.newInstance())
				.commitNow()
		}
	}

	override fun openPhoneAuthForm() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.mainContainer, PhoneLoginFragment.newInstance())
				.commitNow()
		}
	}

	override fun openPhoneCheckForm(params: PhoneAuthParams) {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.replace(R.id.mainContainer, PhoneCheckFragment.newInstance(params))
				.commitNow()
		}
	}

	override fun openSettings() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
				.replace(R.id.modalContainer, SettingsFragment.newInstance())
				.commitNow()
		}
	}

	override fun openChatCreationGroup() {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
				.add(R.id.modalContainer, ChatCreationGroupFragment.newInstance())
				.commitNow()
		}
	}

	override fun openChatCreationDesc(contacts: List<Contact>) {
		runOnUiThread {
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
				.add(R.id.modalContainer, ChatCreationDescFragment.newInstance(contacts))
				.commitNow()
		}
	}

	override fun closeFragment(fragment: Fragment) {
		runOnUiThread {
			hideKeyboard()
			supportFragmentManager.beginTransaction()
				.remove(fragment)
				.commitNow()
		}
	}

	override fun onSuccessfullAuth(user: User) {
		initRepositories(user.id)
		tryObserveDeviceContacts()
		//FirebaseFirestore.getInstance().clearPersistence()
		updateNotificationToken()
		openChatList()
		handleIntent(intent)
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

	override fun requestPermissionAudioMessage(onResult: Runnable1<Boolean>) {
		if (allowedAudioMessage())
			onResult.run(true)
		else {
			permRequestCallback = onResult
			val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
			ActivityCompat.requestPermissions(this, permissions, PERM_AUDIO_MESSAGES)
		}
	}

	override fun allowedAudioMessage(): Boolean {
		return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				&& checkPermission(Manifest.permission.RECORD_AUDIO)
	}

	override fun onContactListUpdate(contacts: List<DeviceContactsManager.Item>) {
		UserRepository.getAll(object : RequestCallback<List<User>> {
			override fun onSuccess(appUsers: List<User>) {

				val updateCandidates = mutableListOf<Contact>()
				val appUsersTmp = LinkedList(appUsers)

				//FIXME in real app its better to switch inner and outer iterators because there will be
				//      much more app users than local contacts. Also, maybe better to
				//      to use UserRepository.getItem on every contacts if the list is small enough
				for (contact in contacts) {

					val appUserIter = appUsersTmp.iterator()
					while (appUserIter.hasNext()) {
						val user = appUserIter.next()

						if (contact.formatedMainNumber == user.phone) {
							updateCandidates.add(Contact(phone = user.phone, name = contact.name))
							appUserIter.remove()
							break
						}
					}
				}

				ContactRepository.update(updateCandidates, object : RequestCallback<Any> {
					override fun onSuccess(result: Any) { Log.d("Intercom", "Success updating contacts") }
					override fun onFailure(error: Throwable) { Utils.logError(error) }
				})
			}
			override fun onFailure(error: Throwable) { Utils.logError(error) }
		})
	}

	private fun initRepositories(userId: String) {
		val dataSourceProvider = App.backend.getDataSourceProvider()
		UserRepository.setRemoteSource(dataSourceProvider.getUserSource(userId))
		ChatRepository.setRemoteSource(dataSourceProvider.getChatSource(userId))
		ContactRepository.setRemoteSource(dataSourceProvider.getContactSource(userId))
	}

	private fun updateNotificationToken() {
		App.backend.getMessagingManager().getToken(object : SimpleRequestCallback<String>() {
			override fun onSuccess(token: String) {
				UserRepository.updateNotificationToken(token, object : SimpleRequestCallback<Any>() {})
			}
		})
	}

	@SuppressLint("MissingPermission")
	private fun tryObserveDeviceContacts(onResult: Runnable1<Boolean>? = null) {
		requestPermissionReadContacts { granted ->
			if (granted)
				observeDeviceContacts()
			onResult?.run(granted)
		}
	}

	@RequiresPermission(Manifest.permission.READ_CONTACTS)
	private fun observeDeviceContacts() {

		DeviceContactsManager.registerUpdateListener(this) //TODO unregister on logout (multiaccount mode)
		DeviceContactsManager.enableObserver(this)

		userContactExecutor.execute { //TODO terminate on logout (multiaccount mode)
			onContactListUpdate(DeviceContactsManager.getContacts(this))
		}
	}

	private fun showToast(@StringRes textResId: Int) {
		runOnUiThread {
			Toast.makeText(this, textResId, Toast.LENGTH_LONG).show()
		}
	}
}

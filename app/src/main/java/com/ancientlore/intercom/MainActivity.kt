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
import com.ancientlore.intercom.C.DEFAULT_LOG_TAG
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.model.call.Offer
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.data.source.UserRepository
import com.ancientlore.intercom.manager.DeviceContactsManager
import com.ancientlore.intercom.ui.Navigator
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginFragment
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckFragment
import com.ancientlore.intercom.ui.boadcast.list.BroadcastListFragment
import com.ancientlore.intercom.ui.call.CallViewModel
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.CallFragment
import com.ancientlore.intercom.ui.call.answer.audio.AudioCallAnswerFragment
import com.ancientlore.intercom.ui.call.offer.audio.AudioCallOfferFragment
import com.ancientlore.intercom.ui.call.answer.video.VideoCallAnswerFragment
import com.ancientlore.intercom.ui.call.offer.video.VideoCallOfferFragment
import com.ancientlore.intercom.ui.chat.creation.ChatCreationFragment
import com.ancientlore.intercom.ui.boadcast.creation.BroadcastCreationFragment
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescFragment
import com.ancientlore.intercom.ui.chat.creation.group.ChatCreationGroupFragment
import com.ancientlore.intercom.ui.chat.detail.ChatDetailFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.ui.chat.list.ChatListFragment
import com.ancientlore.intercom.ui.contact.detail.ContactDetailFragment
import com.ancientlore.intercom.ui.contact.detail.ContactDetailParams
import com.ancientlore.intercom.ui.contact.list.ContactListFragment
import com.ancientlore.intercom.ui.image.viewer.ImageViewerFragment
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
import com.ancientlore.intercom.utils.extensions.openFile
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
		private const val PERM_CALLS = 105

		var isInBackground = false
			private set
	}

	interface BackButtonHandler {
		fun onBackPressed(): Boolean
	}

	private val userContactExecutor = Executors.newSingleThreadExecutor(LoggingThreadFactory("exec_contactUpdate"))

	private val user get() = App.backend.getAuthManager().getCurrentUser()

	private var permRequestCallback: Runnable1<Boolean>? = null

	private var toolbarMenuCallback: Runnable1<Menu>? = null

	private var firstResume: Boolean = true

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

		if (firstResume.not()) {
			if (App.backend.getAuthManager().isLoggedIn()) {
				UserRepository.updateOnlineStatus(true)
			}
		}
		else firstResume = false

		super.onResume()
	}

	override fun onPause() {
		isInBackground = true

		super.onPause()
	}

	override fun onStop() {

		if (App.backend.getAuthManager().isLoggedIn()) {
			UserRepository.updateOnlineStatus(false)
		}
		super.onStop()
	}

	override fun onDestroy() {
		App.backend.getCallManager().dispose()
		DeviceContactsManager.unregisterUpdateListener(this)
		DeviceContactsManager.disableObserver(applicationContext)
		DeviceContactsManager.clean()
		super.onDestroy()
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
		toolbarMenuCallback = null
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
					val fragment = ContactListFragment.newInstance()
					supportFragmentManager.beginTransaction()
						.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
						.replace(R.id.modalContainer, fragment)
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
					val fragment = ChatCreationFragment.newInstance()
					supportFragmentManager.beginTransaction()
						.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
						.replace(R.id.modalContainer, fragment)
						.commitNow()
				}
			} else {
				//TODO show notification that permission is required
			}
		}
	}

	override fun openChatFlow(params: ChatFlowParams) {
		runOnUiThread {
			val fragment = ChatFlowFragment.newInstance(params)
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.replace(R.id.modalContainer, fragment)
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
			val fragment = SettingsFragment.newInstance()
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.replace(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openChatCreationGroup() {
		runOnUiThread {
			val fragment = ChatCreationGroupFragment.newInstance()
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.add(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openChatCreationDesc(contacts: List<Contact>) {
		runOnUiThread {
			val fragment = ChatCreationDescFragment.newInstance(contacts)
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.add(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openChatDetail(params: ChatFlowParams) {
		runOnUiThread {
			val fragment = ChatDetailFragment.newInstance(params)
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.add(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openContactDetail(params: ContactDetailParams) {
		runOnUiThread {
			val fragment = ContactDetailFragment.newInstance(params)
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.add(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openBroadcastList() {
		runOnUiThread {
			val fragment = BroadcastListFragment.newInstance()
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.replace(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openBroadcastCreation() {
		runOnUiThread {
			val fragment = BroadcastCreationFragment.newInstance()
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.add(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openFileViewer(uri: Uri) {
		// TODO maybe create custom file viewer fragment (at least for popular file types)
		if (!openFile(uri)) {
			showToast(R.string.alert_error_open_file)
		}
	}

	override fun openImageViewer(uri: Uri) {
		runOnUiThread {
			val fragment = ImageViewerFragment.newInstance(uri)
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
				.add(R.id.modalContainer, fragment)
				.commitNow()
		}
	}

	override fun openFilePicker(target: Fragment, requestCode: Int) {
		requestPermissionWriteStorage { granted ->
			if (granted) {
				// FIXME temporary solution (TODO add own file explorer)
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("*/*")
				// TODO multiple selection .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
				if (intent.resolveActivity(packageManager) != null)
					startActivityFromFragment(target, intent, requestCode)
				else {
					Utils.logError("No embedded file explorer")
					showToast(R.string.alert_error_no_file_explorer)
				}
			}
		}
	}

	override fun openImagePicker(target: Fragment, requestCode: Int) {
		requestPermissionWriteStorage { granted ->
			if (granted) {
				// FIXME temporary solution (TODO add own gallery)
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("image/*")
				// TODO multiple selection .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
				if (intent.resolveActivity(packageManager) != null)
					startActivityFromFragment(target, intent, requestCode)
				else {
					Utils.logError("No embedded gallery")
					showToast(R.string.alert_error_no_gallery)
				}
			}
		}
	}

	override fun openAudioCallOffer(params: CallViewModel.Params) {
		requestPermissionAudioCalls { granted ->
			if (granted)
				runOnUiThread {
					val fragment = AudioCallOfferFragment.newInstance(params)
					supportFragmentManager.beginTransaction()
						.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
						.add(R.id.modalContainer, fragment)
						.commitNow()
				}
			else showToast(getString(R.string.warn_call_offer_no_perm, params.name))
		}
	}

	override fun openVideoCallOffer(params: CallViewModel.Params) {
		requestPermissionVideoCalls { granted ->
			if (granted)
				runOnUiThread {
					val fragment = VideoCallOfferFragment.newInstance(params)
					supportFragmentManager.beginTransaction()
						.setCustomAnimations(fragment.getOpenAnimation(), fragment.getCloseAnimation())
						.add(R.id.modalContainer, fragment)
						.commitNow()
				}
			else showToast(getString(R.string.warn_call_offer_no_perm, params.name))
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
		UserRepository.updateOnlineStatus(true, object: CrashlyticsRequestCallback<Any>() {
			override fun onSuccess(result: Any) {
				tryObserveDeviceContacts()
			}
		})
		attachCallListener()
		//FirebaseFirestore.getInstance().clearPersistence()
		//val databasesDir = File(context.getApplicationInfo().dataDir.toString() + "/databases")
		//File(databasesDir, "intercom.db").delete()
		updateNotificationToken()
		openChatList()
		handleIntent(intent)
	}

	private fun attachCallListener() {

		App.backend.getCallManager().setIncomingCallHandler { offer ->
			openCallAnswer(offer)
		}
	}

	override fun openCallAnswer(offer: Offer) {
		ContactRepository.getItem(offer.callerId, object: RequestCallback<Contact> {

			override fun onSuccess(result: Contact) {
				openCallAnswerInner(offer, result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				openCallAnswerInner(offer)
			}
		})
	}

	private fun openCallAnswerInner(offer: Offer, contact: Contact? = null) {
		when(offer.callType) {
			Offer.CALL_TYPE_AUDIO -> {
				requestPermissionAudioCalls { granted ->
					if (granted) {
						openCallAnswerInner(
							AudioCallAnswerFragment.newInstance(
								CallAnswerParams(
									offer.callerId,
									offer.sdp,
									contact?.name,
									contact?.iconUrl
								)))
					}
					else showToast(getString(R.string.warn_call_answer_no_perm, contact?.name ?: offer.callerId))
				}
			}
			Offer.CALL_TYPE_VIDEO -> {
				requestPermissionVideoCalls { granted ->
					if (granted) {
						openCallAnswerInner(
							VideoCallAnswerFragment.newInstance(
								CallAnswerParams(
									offer.callerId,
									offer.sdp,
									contact?.name,
									contact?.iconUrl
								)))
					}
					else showToast(getString(R.string.warn_call_answer_no_perm, contact?.name ?: offer.callerId))
				}
			}
			else -> {
				Utils.logError("Call offer from ${offer.callerId} came with unknown callType ${offer.callType}")
			}
		}
	}

	private fun openCallAnswerInner(callFragment: CallFragment<*,*>) {
		runOnUiThread {
			hideKeyboard()
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.center_scale_fade_in, R.anim.center_scale_fade_out)
				.add(R.id.modalContainer, callFragment)
				.commitNow()
		}
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

	fun requestPermissionAudioCalls(onResult: Runnable1<Boolean>) {
		if (allowedAudioCalls())
			onResult.run(true)
		else {
			permRequestCallback = onResult
			val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
			ActivityCompat.requestPermissions(this, permissions, PERM_CALLS)
		}
	}

	fun requestPermissionVideoCalls(onResult: Runnable1<Boolean>) {
		if (allowedVideoCalls())
			onResult.run(true)
		else {
			permRequestCallback = onResult
			val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
			ActivityCompat.requestPermissions(this, permissions, PERM_CALLS)
		}
	}

	override fun allowedAudioMessage(): Boolean {
		return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				&& checkPermission(Manifest.permission.RECORD_AUDIO)
	}

	private fun allowedAudioCalls(): Boolean = checkPermission(Manifest.permission.RECORD_AUDIO)

	private fun allowedVideoCalls(): Boolean {
		return checkPermission(Manifest.permission.CAMERA)
				&& checkPermission(Manifest.permission.RECORD_AUDIO)
	}

	override fun onContactListUpdate(contacts: List<DeviceContactsManager.Item>) {
		UserRepository.getAll(object : CrashlyticsRequestCallback<List<User>>() {
			override fun onSuccess(appUsers: List<User>) {

				userContactExecutor.execute {

					val currentUserId = App.backend.getAuthManager().getCurrentUserId()

					val updateCandidates = mutableListOf<Contact>()
					val appUsersTmp = LinkedList(appUsers)

					//FIXME in real app its better to switch inner and outer iterators because there will be
					//      much more app users than local contacts. Also, maybe better to
					//      to use UserRepository.getItem on every contacts if the list is small enough
					for (contact in contacts) {

						val appUserIter = appUsersTmp.iterator()
						while (appUserIter.hasNext()) {
							val user = appUserIter.next()

							if (user.id != currentUserId  // FIXME currently using yourself as a contact leads to various bugs
								&& contact.formatedMainNumber == user.phone) {
								updateCandidates.add(Contact(
									phone = user.phone,
									name = contact.name,
									iconUrl = user.iconUrl))
								appUserIter.remove()
								break
							}
						}
					}

					ContactRepository.update(updateCandidates, object : CrashlyticsRequestCallback<Any>() {
						override fun onSuccess(result: Any) { Log.d(DEFAULT_LOG_TAG, "Success updating contacts") }
					})
				}
			}
		})
	}

	private fun initRepositories(userId: String) {

		App.backend.getCrashreportManager().setUserId(userId)

		val remoteDataSourceProvider = App.backend.getDataSourceProvider()
		//val localDataSourceProvider = App.frontend.getDataSourceProvider()
		UserRepository.apply {
			setRemoteSource(remoteDataSourceProvider.getUserSource(userId))
			//setLocalSource(localDataSourceProvider.getUserSource(userId))
		}
		ChatRepository.apply {
			setRemoteSource(remoteDataSourceProvider.getChatSource(userId))
			//setLocalSource(localDataSourceProvider.getChatSource(userId))
		}
		ContactRepository.apply {
			setRemoteSource(remoteDataSourceProvider.getContactSource(userId))
			//setLocalSource(localDataSourceProvider.getContactSource(userId))
		}
	}

	private fun updateNotificationToken() {
		App.backend.getMessagingManager().getToken(object : CrashlyticsRequestCallback<String>() {
			override fun onSuccess(token: String) {
				UserRepository.updateNotificationToken(token)
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
		DeviceContactsManager.enableObserver(applicationContext)

		userContactExecutor.execute {
			onContactListUpdate(DeviceContactsManager.getContacts(this))
		}
	}

	private fun showToast(@StringRes textResId: Int) {
		runOnUiThread {
			Toast.makeText(this, textResId, Toast.LENGTH_LONG).show()
		}
	}

	private fun showToast(text: String) {
		runOnUiThread {
			Toast.makeText(this, text, Toast.LENGTH_LONG).show()
		}
	}
}

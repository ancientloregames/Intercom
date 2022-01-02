package com.ancientlore.intercom.ui.settings

import android.net.Uri
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.ProgressRequestCallback
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.FileData
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.UserRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import com.ancientlore.intercom.view.TextDrawable
import java.util.regex.Pattern


class SettingsViewModel(private val user: User)
	: BasicViewModel() {

	companion object {
		const val TOAST_INVALID_NAME = 0
		const val TOAST_INVALID_STATUS = 1
		const val TOAST_ERR_CHANGE_NAME = 2
		const val TOAST_ERR_CHANGE_STATUS = 3
	}

	val userIconField = ObservableField<Any>(user.iconUri)
	val userNameField = ObservableField(user.name)
	val userStatusField = ObservableField(user.status)

	private val openGallerySub = PublishSubject.create<Any>()

	private val openImageViewerSubj = PublishSubject.create<Uri>()

	private val showNameEditorSubj = PublishSubject.create<String>()

	private val showStatusEditorSubj = PublishSubject.create<String>()

	private val openAppSettingsSubj = PublishSubject.create<Any>()

	private val openNotificationSettingsSubj = PublishSubject.create<Any>()

	@Px
	private var abbrSize: Int = 0
	@ColorInt
	private var abbrColor: Int = 0

	fun init(abbrSize: Int, abbrColor: Int) {

		this.abbrSize = abbrSize
		this.abbrColor = abbrColor

		if (user.iconUrl.isEmpty()) {
			userIconField.set(ImageUtils.createAbbreviationDrawable(user.name, abbrColor, abbrSize))
		}
	}

	override fun clean() {
		openGallerySub.onComplete()
		openImageViewerSubj.onComplete()
		showNameEditorSubj.onComplete()
		showStatusEditorSubj.onComplete()
		openAppSettingsSubj.onComplete()
		openNotificationSettingsSubj.onComplete()

		super.clean()
	}

	fun onUserIconClicked() {
		userIconField.get()
			?.takeIf { it is Uri && it != Uri.EMPTY }
			?.let { openImageViewerSubj.onNext(it as Uri) }
	}

	fun onSetProfilePhotoClicked() = openGallerySub.onNext(EmptyObject)

	fun onChangeUserNameClicked() {
		showNameEditorSubj.onNext(userNameField.get()!!)
	}

	fun onChangeUserStatusClicked() {
		showStatusEditorSubj.onNext(userStatusField.get()!!)
	}

	fun onAppSettingsButtonClicked() {
		openAppSettingsSubj.onNext(EmptyObject)
	}

	fun onNotificationsButtonClicked() {
		openNotificationSettingsSubj.onNext(EmptyObject)
	}

	fun observeOpenGalleryRequest() = openGallerySub as Observable<Any>

	fun openImageViewerRequest() = openImageViewerSubj as Observable<Uri>

	fun showNameEditorRequest() = showNameEditorSubj as Observable<String>

	fun showStatusEditorRequest() = showStatusEditorSubj as Observable<String>

	fun openAppSettingsRequest() = openAppSettingsSubj as Observable<Any>

	fun openNotificationSettingsRequest() = openNotificationSettingsSubj as Observable<Any>

	fun handleSelectedProfileIcon(fileData: FileData) {

		// TODO show loading screen to block other actions
		App.backend.getStorageManager().uploadImage(fileData, user.id, object : ProgressRequestCallback<Uri> {
			override fun onProgress(progress: Int) {
				// TODO show progress
			}
			override fun onSuccess(uri: Uri) {
				UserRepository.updateIcon(uri, object : SimpleRequestCallback<Any>() {
					override fun onSuccess(result: Any) {
						// TODO hide loading screen
						runOnUiThread {
							userIconField.set(uri)
						}
					}
					override fun onFailure(error: Throwable) {
						// TODO hide loading screen
						Utils.logError(error)
					}
				})
			}
			override fun onFailure(error: Throwable) {
				// TODO hide loading screen
				Utils.logError(error)
			}
		})
	}

	private fun validateUserName(name: String) : Boolean {
		return name.length in 5..64 && Pattern.matches("[a-zA-Z0-9_]+", name)
	}

	fun updateUserName(newUserName: String) {

		if (validateUserName(newUserName)) {

			UserRepository.updateName(newUserName, object : SimpleRequestCallback<Any>() {
				override fun onSuccess(result: Any) {

					runOnUiThread {
						userNameField.set(newUserName)

						val icon = userIconField.get()
						if (icon is TextDrawable || icon == Uri.EMPTY)
							userIconField.set(ImageUtils.createAbbreviationDrawable(newUserName, abbrColor, abbrSize))
					}
				}
				override fun onFailure(error: Throwable) {
					Utils.logError(error)
					toastRequest.onNext(TOAST_ERR_CHANGE_NAME)
				}
			})
		}
		else toastRequest.onNext(TOAST_INVALID_NAME)
	}

	private fun validateUserStatus(name: String) : Boolean {
		return name.length < 256
	}

	fun updateUserStatus(newStatus: String) {

		if (validateUserStatus(newStatus)) {

			UserRepository.updateStatus(newStatus, object : SimpleRequestCallback<Any>() {
				override fun onSuccess(result: Any) {

					runOnUiThread {
						userStatusField.set(newStatus)
					}
				}
				override fun onFailure(error: Throwable) {
					Utils.logError(error)
					toastRequest.onNext(TOAST_ERR_CHANGE_STATUS)
				}
			})
		}
		else toastRequest.onNext(TOAST_INVALID_STATUS)
	}
}
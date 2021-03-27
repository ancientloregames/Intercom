package com.ancientlore.intercom.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.R
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
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.ancientlore.intercom.utils.extensions.showKeyboard
import com.ancientlore.intercom.view.TextDrawable
import java.util.regex.Pattern


class SettingsViewModel(private val user: User)
	: BasicViewModel() {

	val userIcon = ObservableField<Any>(user.iconUri)
	val userName = ObservableField(user.name)

	private val openGallerySub = PublishSubject.create<Any>()

	@Px
	private var abbrSize: Int = 0
	@ColorInt
	private var abbrColor: Int = 0

	private var editNameField: EditText? = null
	private var editNameDialog: AlertDialog? = null

	fun init(context: Context) {
		if (user.iconUrl.isEmpty()) {
			abbrSize = context.resources.getDimensionPixelSize(R.dimen.settingsUserNameAbSize)
			abbrColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
			userIcon.set(ImageUtils.createAbbreviationDrawable(user.name, abbrColor, abbrSize))
		}

		editNameField = EditText(context).apply {
			setHint(R.string.dialog_edit_user_name_hint)
			setText(user.name)
		}
		editNameDialog = AlertDialog.Builder(context)
			.setTitle(R.string.dialog_edit_user_name_title)
			.setMessage(R.string.dialog_edit_user_name_message)
			.setView(editNameField)
			.setPositiveButton(R.string.ok) { _, _ ->
				val text = editNameField!!.text.toString()
				if (validateUserName(text))
					updateUserName(text)
				else
					toastRequest.onNext(R.string.alert_error_invalid_name)
			}
			.setNegativeButton(R.string.cancel, null)
			.create()
	}

	override fun clean() {
		openGallerySub.onComplete()

		super.clean()
	}

	fun onSetProfilePhotoClicked() = openGallerySub.onNext(EmptyObject)

	fun onChangeUserNameClicked() {
		editNameField?.setText(userName.get())
		editNameDialog?.show()
		Utils.runOnUiThread({ editNameField?.showKeyboard() }, 20)
	}

	fun observeOpenGalleryRequest() = openGallerySub as Observable<*>

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
						userIcon.set(uri)
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

	private fun updateUserName(newUserName: String) {
		UserRepository.updateName(newUserName, object : SimpleRequestCallback<Any>() {
			override fun onSuccess(result: Any) {

				userName.set(newUserName)

				val icon = userIcon.get()
				if (icon is TextDrawable || icon == Uri.EMPTY)
					userIcon.set(ImageUtils.createAbbreviationDrawable(newUserName, abbrColor, abbrSize))
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				toastRequest.onNext(R.string.alert_error_change_name)
			}
		})
	}
}
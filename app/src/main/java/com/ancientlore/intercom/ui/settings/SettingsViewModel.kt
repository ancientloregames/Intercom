package com.ancientlore.intercom.ui.settings

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
import androidx.annotation.ColorInt
import androidx.annotation.Px


class SettingsViewModel(private val user: User)
	: BasicViewModel() {

	val userIcon = ObservableField<Any>(user.iconUri)
	val userName = ObservableField(user.name)

	private val openGallerySub = PublishSubject.create<Any>()

	@Px
	private var abbrSize: Int = 0
	@ColorInt
	private var abbrColor: Int = 0

	fun init(context: Context) {
		if (user.iconUrl.isEmpty()) {
			abbrSize = context.resources.getDimensionPixelSize(R.dimen.settingsUserNameAbSize)
			abbrColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
			userIcon.set(ImageUtils.createAbbreviationDrawable(user.name, abbrColor, abbrSize))
		}
	}

	override fun clean() {
		openGallerySub.onComplete()

		super.clean()
	}

	fun onSetProfilePhotoClicked() = openGallerySub.onNext(EmptyObject)

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
}
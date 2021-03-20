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
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class SettingsViewModel(private val user: User)
	: BasicViewModel() {

	val userIcon = ObservableField<Any>(user.iconUri)

	private val openGallerySub = PublishSubject.create<Any>()

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
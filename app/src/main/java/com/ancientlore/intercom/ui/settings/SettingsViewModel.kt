package com.ancientlore.intercom.ui.settings

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class SettingsViewModel : BasicViewModel() {

	private val openGallerySub = PublishSubject.create<Any>()

	override fun clean() {
		openGallerySub.onComplete()

		super.clean()
	}

	fun onSetProfilePhotoClicked() = openGallerySub.onNext(EmptyObject)

	fun observeOpenGalleryRequest() = openGallerySub as Observable<*>
}
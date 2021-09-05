package com.ancientlore.intercom.ui.image.viewer

import android.net.Uri
import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ImageViewerViewModel(uri: Uri): BasicViewModel() {

	val iconUrlField = ObservableField(uri)

	private val closeSubj = PublishSubject.create<Any>()

	override fun clean() {
		closeSubj.onComplete()
		super.clean()
	}

	fun onBackClicked() = closeSubj.onNext(EmptyObject)

	fun closeRequest() = closeSubj as Observable<Any>
}
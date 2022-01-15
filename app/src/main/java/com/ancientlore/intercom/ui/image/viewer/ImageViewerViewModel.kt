package com.ancientlore.intercom.ui.image.viewer

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ImageViewerViewModel @Inject constructor(
	params: Params
): BasicViewModel() {

	val iconUrlField = ObservableField(params.uri)

	private val closeSubj = PublishSubject.create<Any>()

	override fun clean() {
		closeSubj.onComplete()
		super.clean()
	}

	fun onBackClicked() = closeSubj.onNext(EmptyObject)

	fun closeRequest() = closeSubj as Observable<Any>

	data class Params(val uri: Uri): Parcelable {
		constructor(parcel: Parcel) : this(parcel.readParcelable(Uri::class.java.classLoader))

		override fun writeToParcel(parcel: Parcel, flags: Int) {
			parcel.writeParcelable(uri, flags)
		}

		override fun describeContents(): Int = 0

		companion object CREATOR : Parcelable.Creator<Params> {
			override fun createFromParcel(parcel: Parcel): Params = Params(parcel)

			override fun newArray(size: Int): Array<Params?> = arrayOfNulls(size)
		}
	}
}
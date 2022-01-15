package com.ancientlore.intercom.ui.call

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CallConnectionListener
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class CallViewModel(internal val params: Params) : BasicViewModel(), CallConnectionListener {

	val collocutorNameField = ObservableField(params.name ?: params.targetId)

	val collocutorIconField = ObservableField(params.iconUrl)

	private val startChronometerSubj = PublishSubject.create<Any>()

	private val hangupSubj = PublishSubject.create<Any>()

	protected var conversationStarted: Boolean = false

	@CallSuper
	override fun onConnected() {
		conversationStarted = true
		startChronometerSubj.onNext(EmptyObject)
	}

	override fun onDisconnected() {
		onHangupCall()
	}

	fun onHangupCall() = hangupSubj.onNext(EmptyObject)

	fun startChronometerRequest() = startChronometerSubj as Observable<Any>

	fun hangupCallRequest() = hangupSubj as Observable<Any>

	open class Params(open val targetId: String,
	                  open val name: String? = null,
	                  open val iconUrl: String? = null): Parcelable {

		constructor(parcel: Parcel) : this(
			parcel.readString(),
			parcel.readString(),
			parcel.readString()
		)

		override fun writeToParcel(parcel: Parcel, flags: Int) {
			parcel.writeString(targetId)
			parcel.writeString(name)
			parcel.writeString(iconUrl)
		}

		override fun describeContents(): Int {
			return 0
		}

		companion object CREATOR : Parcelable.Creator<Params> {
			override fun createFromParcel(parcel: Parcel): Params {
				return Params(parcel)
			}

			override fun newArray(size: Int): Array<Params?> {
				return arrayOfNulls(size)
			}
		}
	}
}
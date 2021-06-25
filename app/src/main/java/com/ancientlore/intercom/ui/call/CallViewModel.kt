package com.ancientlore.intercom.ui.call

import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CallConnectionListener
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class CallViewModel(conterpartId: String) : BasicViewModel(), CallConnectionListener {

	val conterpartField = ObservableField(conterpartId)

	init {
		// TODO find caller name in contacts cache
	}

	override fun onDisconnected() {
		onHangupCall()
	}

	private val hangupSubj = PublishSubject.create<Any>()

	fun onHangupCall() = hangupSubj.onNext(EmptyObject)

	fun observeHangupCall() = hangupSubj as Observable<Any>
}
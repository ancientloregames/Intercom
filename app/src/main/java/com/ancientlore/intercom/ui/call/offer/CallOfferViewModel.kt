package com.ancientlore.intercom.ui.call.offer

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.call.CallViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class CallOfferViewModel(params: Params) : CallViewModel(params) {

	private val stopCallSoundSubj = PublishSubject.create<Any>()

	override fun clean() {
		stopCallSoundSubj.onComplete()

		super.clean()
	}

	override fun onConnected() {
		super.onConnected()
		stopCallSoundSubj.onNext(EmptyObject)
	}

	fun stopCallSoundRequest() = stopCallSoundSubj as Observable<Any>
}
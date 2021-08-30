package com.ancientlore.intercom.ui.call.answer.audio

import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AudioCallAnswerViewModel(params: CallAnswerParams)
	: CallAnswerViewModel(params) {

	private val turnOnProximitySensorSubj = PublishSubject.create<Any>()

	override fun onConnected() {
		super.onConnected()
		turnOnProximitySensorSubj.onNext(EmptyObject)
	}

	fun turnOnProximitySensorRequest() = turnOnProximitySensorSubj as Observable<Any>

}
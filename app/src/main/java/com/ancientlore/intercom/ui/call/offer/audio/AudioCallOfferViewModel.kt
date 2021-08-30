package com.ancientlore.intercom.ui.call.offer.audio

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.call.offer.CallOfferViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AudioCallOfferViewModel(params: Params)
	: CallOfferViewModel(params) {

	private val turnOnProximitySensorSubj = PublishSubject.create<Any>()

	override fun onConnected() {
		super.onConnected()
		turnOnProximitySensorSubj.onNext(EmptyObject)
	}

	fun turnOnProximitySensorRequest() = turnOnProximitySensorSubj as Observable<Any>
}
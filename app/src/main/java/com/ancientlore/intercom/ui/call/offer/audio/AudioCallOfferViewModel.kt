package com.ancientlore.intercom.ui.call.offer.audio

import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.ui.call.offer.CallOfferViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

class AudioCallOfferViewModel @Inject constructor(
	@Named("AudioCallOffer") params: Params
) : CallOfferViewModel(params) {

	private val turnOnProximitySensorSubj = PublishSubject.create<Any>()

	init {
		App.backend.getCallManager().apply {
			setCallConnectionListener(this@AudioCallOfferViewModel)
			call(
				CallManager.AudioCallParams(
					params.targetId
				))
		}
	}

	override fun onConnected() {
		super.onConnected()
		turnOnProximitySensorSubj.onNext(EmptyObject)
	}

	fun turnOnProximitySensorRequest() = turnOnProximitySensorSubj as Observable<Any>
}
package com.ancientlore.intercom.ui.call.answer.audio

import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

class AudioCallAnswerViewModel @Inject constructor(
	@Named("AudioCallAnswer") params: CallAnswerParams
) : CallAnswerViewModel(params) {

	private val turnOnProximitySensorSubj = PublishSubject.create<Any>()

	override fun answer() {
		App.backend.getCallManager().apply {
			setCallConnectionListener(this@AudioCallAnswerViewModel)
			answer(
				CallManager.AudioCallParams(
					params.targetId), (params as CallAnswerParams).sdp)
		}
	}

	override fun onConnected() {
		super.onConnected()
		turnOnProximitySensorSubj.onNext(EmptyObject)
	}

	fun turnOnProximitySensorRequest() = turnOnProximitySensorSubj as Observable<Any>

}
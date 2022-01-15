package com.ancientlore.intercom.ui.call.offer.video

import androidx.databinding.ObservableBoolean
import com.ancientlore.intercom.ui.call.offer.CallOfferViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

class VideoCallOfferViewModel @Inject constructor(
	@Named("VideoCallOffer") params: Params
) : CallOfferViewModel(params) {

	val showIconField = ObservableBoolean(true)

	private val showHUDSubj = PublishSubject.create<Boolean>()

	private var showHUD = true

	override fun onConnected() {
		super.onConnected()
		showIconField.set(false)
	}

	fun onScreenClick() {
		if (conversationStarted) {
			showHUD = !showHUD
			showHUDSubj.onNext(showHUD)
		}
	}

	fun showHUDRequest() = showHUDSubj as Observable<Boolean>
}
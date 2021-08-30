package com.ancientlore.intercom.ui.call.answer.video

import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class VideoCallAnswerViewModel(params: CallAnswerParams)
	: CallAnswerViewModel(params) {

	private val showHUDSubj = PublishSubject.create<Boolean>()

	private var showHUD = true

	fun onScreenClick() {
		if (conversationStarted) {
			showHUD = !showHUD
			showHUDSubj.onNext(showHUD)
		}
	}

	fun showHUDRequest() = showHUDSubj as Observable<Boolean>
}
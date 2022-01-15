package com.ancientlore.intercom.ui.call.answer.video

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.answer.CallAnswerViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

class VideoCallAnswerViewModel @Inject constructor(
	@Named("VideoCallAnswer") params: CallAnswerParams
) : CallAnswerViewModel(params) {

	private val showHUDSubj = PublishSubject.create<Boolean>()

	private val makeCallSubj = PublishSubject.create<Any>()

	private var showHUD = true

	override fun answer() {
		makeCallSubj.onNext(EmptyObject)
	}

	fun onScreenClick() {
		if (conversationStarted) {
			showHUD = !showHUD
			showHUDSubj.onNext(showHUD)
		}
	}

	fun showHUDRequest() = showHUDSubj as Observable<Boolean>

	fun makeCallRequest() = makeCallSubj as Observable<Any>
}
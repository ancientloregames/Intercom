package com.ancientlore.intercom.ui.call.answer

import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.CallViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class CallAnswerViewModel(params: CallAnswerParams) : CallViewModel(params) {

	val showAnswerPanelField = ObservableField(true)

	private val answerCallSubj = PublishSubject.create<Any>()
	private val declineCallSubj = PublishSubject.create<Any>()

	override fun onConnected() {
		super.onConnected()
		showAnswerPanelField.set(false)
	}

	fun onAnswerCall() {
		answerCallSubj.onNext(EmptyObject)
	}

	fun onDeclineCall() = declineCallSubj.onNext(EmptyObject)

	fun answerCallRequest() = answerCallSubj as Observable<Any>

	fun declineCallRequest() = declineCallSubj as Observable<Any>
}
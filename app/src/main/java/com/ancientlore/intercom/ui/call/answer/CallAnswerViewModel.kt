package com.ancientlore.intercom.ui.call.answer

import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.call.CallViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CallAnswerViewModel(callerId: String) : CallViewModel(callerId) {

	val showControllPanelField = ObservableField(true)

	private val answerCallSubj = PublishSubject.create<Any>()
	private val declineCallSubj = PublishSubject.create<Any>()

	override fun onConnected() {
		showControllPanelField.set(false)
	}

	fun onAnswerCall() {
		answerCallSubj.onNext(EmptyObject)
	}

	fun onDeclineCall() = declineCallSubj.onNext(EmptyObject)

	fun observeAnswerCall() = answerCallSubj as Observable<Any>

	fun observeDeclineCall() = declineCallSubj as Observable<Any>
}
package com.ancientlore.intercom.ui.call.answer

import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.call.CallAnswerParams
import com.ancientlore.intercom.ui.call.CallViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class CallAnswerViewModel(params: CallAnswerParams) : CallViewModel(params) {

	val showAnswerPanelField = ObservableField(true)

	private val stopCallSoundSubj = PublishSubject.create<Any>()
	private val closeSubj = PublishSubject.create<Any>()

	abstract fun answer()

	override fun onConnected() {
		super.onConnected()
		showAnswerPanelField.set(false)
	}

	fun onAnswerCall() {
		stopCallSoundSubj.onNext(EmptyObject)
		answer()
	}

	fun onDeclineCall() {
		App.backend.getCallManager().hungup()
		closeSubj.onNext(EmptyObject)
	}

	fun stopCallSoundRequest() = stopCallSoundSubj as Observable<Any>

	fun closeRequest() = closeSubj as Observable<Any>
}
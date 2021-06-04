package com.ancientlore.intercom.ui.dialog.option.chat

import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatOptionMenuViewModel(val params: ChatOptionMenuParams)
	: BasicViewModel() {

	private val onPinSubj = PublishSubject.create<Boolean>()

	override fun clean() {
		onPinSubj.onComplete()

		super.clean()
	}

	fun onPinClick() = onPinSubj.onNext(params.pin.not())

	fun observePinClicked() = onPinSubj as Observable<Boolean>
}
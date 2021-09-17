package com.ancientlore.intercom.ui.dialog.option.chat

import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatOptionMenuViewModel(val params: ChatOptionMenuParams)
	: BasicViewModel() {

	private val onPinSubj = PublishSubject.create<Boolean>()
	private val onMuteSubj = PublishSubject.create<Boolean>()
	private val onDeleteSubj = PublishSubject.create<Boolean>()

	override fun clean() {
		onPinSubj.onComplete()
		onMuteSubj.onComplete()
		onDeleteSubj.onComplete()

		super.clean()
	}

	fun onPinClick() = onPinSubj.onNext(params.pin.not())

	fun onMuteClick() = onMuteSubj.onNext(params.mute.not())

	fun onDeleteClick() = onDeleteSubj.onNext(params.mute.not())

	fun observePinClicked() = onPinSubj as Observable<Boolean>

	fun observeMuteClicked() = onMuteSubj as Observable<Boolean>

	fun deleteRequest() = onDeleteSubj as Observable<Boolean>
}
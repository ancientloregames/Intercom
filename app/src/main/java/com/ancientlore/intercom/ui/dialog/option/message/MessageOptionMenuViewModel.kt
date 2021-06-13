package com.ancientlore.intercom.ui.dialog.option.message

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class MessageOptionMenuViewModel
	: BasicViewModel() {

	private val onDeleteSubj = PublishSubject.create<Any>()

	override fun clean() {
		onDeleteSubj.onComplete()

		super.clean()
	}

	fun onDeleteClick() = onDeleteSubj.onNext(EmptyObject)

	fun observeDeleteClicked() = onDeleteSubj as Observable<Any>
}
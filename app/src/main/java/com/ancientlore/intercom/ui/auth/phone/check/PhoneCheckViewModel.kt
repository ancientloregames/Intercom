package com.ancientlore.intercom.ui.auth.phone.check

import androidx.databinding.ObservableField
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PhoneCheckViewModel : BasicViewModel() {

	val codeField = ObservableField<String>("")

	private val code get() = codeField.get()!!

	private val enterRequest = PublishSubject.create<String>()

	fun onEnterClicked() {
		enterRequest.onNext(code)
	}

	fun observeEnterRequest() = enterRequest as Observable<String>
}
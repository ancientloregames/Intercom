package com.ancientlore.intercom.ui.auth.phone.login

import androidx.databinding.ObservableField
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PhoneLoginViewModel : BasicViewModel() {

	companion object {
		const val VALIDITY_OK = 0
		const val VALIDITY_EMPTY_PHONE = 1
	}

	val phoneField = ObservableField<String>("")

	private val userPhone get() = phoneField.get()!!

	private val validationCode: Int = when {
		userPhone.isEmpty() -> VALIDITY_EMPTY_PHONE
		else -> VALIDITY_OK
	}

	private val enterClickedEvent = PublishSubject.create<String>()
	private val alertEvent = PublishSubject.create<Int>()

	fun onEnterClicked() {
		when (validationCode) {
			VALIDITY_OK -> enterClickedEvent.onNext(userPhone)
			else -> alertEvent.onNext(validationCode)
		}
	}

	fun observeEnterClickedEvent() = enterClickedEvent as Observable<String>
}
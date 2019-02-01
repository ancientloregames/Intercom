package com.ancientlore.intercom.ui.auth.phone.login

import androidx.databinding.ObservableField
import com.ancientlore.intercom.data.model.Phone
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PhoneLoginViewModel : BasicViewModel() {

	companion object {
		const val VALIDITY_OK = 0
		const val ERROR_NO_PHONE = 1
		const val ERROR_INVALID_PHONE = 2
	}

	val phoneField = ObservableField<String>("")

	private val userPhone get() = phoneField.get()!!

	private val validationCode: Int = when {
		userPhone.isEmpty() -> ERROR_NO_PHONE
		isValidPhone().not() -> ERROR_INVALID_PHONE
		else -> VALIDITY_OK
	}

	private val phoneEnteredEvent = PublishSubject.create<Phone>()
	private val alertRequestEvent = PublishSubject.create<Int>()

	fun onEnterClicked() {
		when (validationCode) {
			VALIDITY_OK -> phoneEnteredEvent.onNext(Phone(userPhone))
			else -> alertRequestEvent.onNext(validationCode)
		}
	}

	fun observePhoneEnteredEvent() = phoneEnteredEvent as Observable<Phone>

	fun observeAlertRequestEvent() = alertRequestEvent as Observable<Int>

	private fun isValidPhone() : Boolean {
		// TODO
		return true
	}
}
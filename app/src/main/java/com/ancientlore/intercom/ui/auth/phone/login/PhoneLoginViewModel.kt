package com.ancientlore.intercom.ui.auth.phone.login

import androidx.databinding.ObservableField
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.ui.auth.AuthViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PhoneLoginViewModel : AuthViewModel() {

	companion object {
		const val VALIDITY_OK = 0
		const val ERROR_NO_PHONE = 1
		const val ERROR_INVALID_PHONE = 2
		const val ERROR_AUTH_FAILED = 3
	}

	val phoneField = ObservableField<String>("")

	private val userPhone get() = phoneField.get()!!

	private val loginSuccessEvent = PublishSubject.create<User>()
	private val validationRequestEvent = PublishSubject.create<PhoneAuthParams>()
	private val alertRequestEvent = PublishSubject.create<Int>()

	fun onEnterClicked() {
		when (val validityCode = getFieldsValidityCode()) {
			VALIDITY_OK -> login()
			else -> alertRequestEvent.onNext(validityCode)
		}
	}

	fun observeLoginSuccessEvent() = loginSuccessEvent as Observable<User>

	fun observeValidationRequestEvent() = validationRequestEvent as Observable<PhoneAuthParams>

	fun observeAlertRequestEvent() = alertRequestEvent as Observable<Int>

	private fun getFieldsValidityCode(): Int {
		return when {
			userPhone.isEmpty() -> ERROR_NO_PHONE
			isValidPhone().not() -> ERROR_INVALID_PHONE
			else -> VALIDITY_OK
		}
	}

	private fun isValidPhone() : Boolean {
		// TODO
		return true
	}

	private fun login() {
		val number = userPhone
		authManager.loginViaPhone(PhoneAuthParams(number), object : RequestCallback<User> {
			override fun onSuccess(result: User) {
				if (authManager.isNeedPhoneCheck())
					validationRequestEvent.onNext(PhoneAuthParams(number))
				else
					loginSuccessEvent.onNext(result)
			}
			override fun onFailure(error: Throwable) {
				error.printStackTrace()
				alertRequestEvent.onNext(ERROR_AUTH_FAILED)
			}
		})
	}
}
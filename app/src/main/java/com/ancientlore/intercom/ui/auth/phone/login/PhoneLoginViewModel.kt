package com.ancientlore.intercom.ui.auth.phone.login

import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.ui.auth.AuthViewModel
import com.ancientlore.intercom.utils.Utils
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

	private val userPhone get() = Utils.formatPhoneNumber(phoneField.get()!!)

	private val loginSuccessEvent = PublishSubject.create<User>()
	private val validationRequestEvent = PublishSubject.create<PhoneAuthParams>()

	fun onEnterClicked() {
		when (val validityCode = getFieldsValidityCode()) {
			VALIDITY_OK -> login()
			else -> alertRequestSub.onNext(validityCode)
		}
	}

	fun observeLoginSuccessEvent() = loginSuccessEvent as Observable<User>

	fun observeValidationRequestEvent() = validationRequestEvent as Observable<PhoneAuthParams>

	private fun getFieldsValidityCode(): Int {
		return when {
			userPhone.isEmpty() -> ERROR_NO_PHONE
			isValidPhone().not() -> ERROR_INVALID_PHONE
			else -> VALIDITY_OK
		}
	}

	private fun isValidPhone() : Boolean {
		return userPhone.run { matches(Regex("\\+[0-9]+")) && length > 3 && length < 14 }
	}

	private fun login() {
		val rawNumber = userPhone
		val number = if (rawNumber.startsWith('+')) rawNumber else "+$rawNumber"
		authManager.loginViaPhone(PhoneAuthParams(number), object : AuthCallback {
			override fun onVerification(id: String) {
				if (authManager.isLoggedIn())
					loginSuccessEvent.onNext(authManager.getCurrentUser())
				else
					validationRequestEvent.onNext(PhoneAuthParams(number, id))
			}
			override fun onSuccess(result: User) {
				loginSuccessEvent.onNext(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				alertRequestSub.onNext(ERROR_AUTH_FAILED)
			}
		})
	}
}
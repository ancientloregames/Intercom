package com.ancientlore.intercom.ui.auth.email.signup

import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class EmailSignupViewModel: BasicViewModel() {

	companion object {
		const val VALIDITY_OK = 0
		const val ERROR_EMPTY_FIELDS = 1
		const val ERROR_NO_EMAIL = 2
		const val ERROR_NO_PASS = 3
	}

	val emailField = ObservableField<String>("")
	val passField = ObservableField<String>("")

	private val userEmail get() = emailField.get()!!
	private val userPass get() = passField.get()!!

	private val alertRequestEvent = PublishSubject.create<Int>()
	private val signupRequest = PublishSubject.create<EmailAuthParams>()
	private val loginRequest = PublishSubject.create<Any>()

	private val credentials get() = composeParams()

	fun observeAlertRequest() = alertRequestEvent as Observable<Int>
	fun observeSignupRequest() = signupRequest as Observable<EmailAuthParams>
	fun observeLoginRequest() = loginRequest as Observable<*>

	fun onSignupClicked() {
		when (val validityCode = getFieldsValidityCode()) {
			VALIDITY_OK -> signupRequest.onNext(credentials)
			else -> alertRequestEvent.onNext(validityCode)
		}
	}

	fun onLoginClicked() = loginRequest.onNext(EmptyObject)

	private fun composeParams() = EmailAuthParams(userEmail, userPass)

	private fun getFieldsValidityCode(): Int {
		return when {
			userEmail.isEmpty() && userPass.isEmpty() -> ERROR_EMPTY_FIELDS
			userEmail.isEmpty() -> ERROR_NO_EMAIL
			userPass.isEmpty() -> ERROR_NO_PASS
			else -> VALIDITY_OK
		}
	}
}
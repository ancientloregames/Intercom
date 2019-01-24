package com.ancientlore.intercom.ui.auth.email.login

import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class EmailLoginViewModel: BasicViewModel() {

	companion object {
		const val VALIDITY_OK = 0
		const val VALIDITY_EMPTY_FIELDS = 1
		const val VALIDITY_EMPTY_ID = 2
		const val VALIDITY_EMPTY_PASS = 3
	}

	val emailField = ObservableField<String>("")
	val passField = ObservableField<String>("")

	private val credentials get() = composeLoginParams()
	private val isValid get() = validateFields()

	private val userEmail get() = emailField.get()!!
	private val userPass get() = passField.get()!!

	private val alertRequest = PublishSubject.create<Int>()
	private val signupRequest = PublishSubject.create<Any>()
	private val loginRequest = PublishSubject.create<EmailAuthParams>()

	fun onSignupClicked() = signupRequest.onNext(EmptyObject)

	fun onLoginClicked() {
		if (isValid)
			loginRequest.onNext(credentials)
	}

	fun observeAlertRequest() = alertRequest as Observable<Int>
	fun observeSignupRequest() = signupRequest as Observable<*>
	fun observeLoginRequest() = loginRequest as Observable<EmailAuthParams>

	private fun validateFields(): Boolean {
		val validationCode = checkValidity()

		return if (validationCode != VALIDITY_OK) {
			alertRequest.onNext(validationCode)
			false
		} else true
	}

	private fun checkValidity(): Int {
		return when {
			userEmail.isEmpty() && userPass.isEmpty() -> VALIDITY_EMPTY_FIELDS
			userEmail.isEmpty() -> VALIDITY_EMPTY_ID
			userPass.isEmpty() -> VALIDITY_EMPTY_PASS
			else -> VALIDITY_OK
		}
	}

	private fun composeLoginParams() = EmailAuthParams(userEmail, userPass)
}
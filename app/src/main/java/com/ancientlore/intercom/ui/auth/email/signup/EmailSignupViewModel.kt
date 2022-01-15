package com.ancientlore.intercom.ui.auth.email.signup

import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.ui.auth.AuthViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class EmailSignupViewModel @Inject constructor()
	: AuthViewModel() {

	companion object {
		const val VALIDITY_OK = 0
		const val ERROR_NO_NAME = 1
		const val ERROR_NO_EMAIL = 2
		const val ERROR_NO_PASS = 3
		const val ERROR_PASS2 = 4
	}

	val nameField = ObservableField<String>("")
	val emailField = ObservableField<String>("")
	val passField = ObservableField<String>("")
	val confirmField = ObservableField<String>("")

	private val userName get() = emailField.get()!!
	private val userEmail get() = emailField.get()!!
	private val userPass get() = passField.get()!!
	private val userPass2 get() = emailField.get()!!

	private val signupRequest = PublishSubject.create<EmailAuthParams>()
	private val loginRequest = PublishSubject.create<Any>()

	private val credentials get() = composeParams()

	fun observeSignupRequest() = signupRequest as Observable<EmailAuthParams>
	fun observeLoginRequest() = loginRequest as Observable<*>

	fun onSignupClicked() {
		when (val validityCode = getFieldsValidityCode()) {
			VALIDITY_OK -> signupRequest.onNext(credentials)
			else -> alertRequestSub.onNext(validityCode)
		}
	}

	fun onLoginClicked() = loginRequest.onNext(EmptyObject)

	private fun composeParams() = EmailAuthParams(userEmail, userPass)

	private fun getFieldsValidityCode(): Int {
		return when {
			userName.isEmpty() -> ERROR_NO_NAME
			userEmail.isEmpty() -> ERROR_NO_EMAIL
			userPass.isEmpty() -> ERROR_NO_PASS
			userPass2 != userPass -> ERROR_PASS2
			else -> VALIDITY_OK
		}
	}
}
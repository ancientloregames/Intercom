package com.ancientlore.intercom.ui.auth.email.signup

import androidx.databinding.ObservableField
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class EmailSignupViewModel: BasicViewModel() {

	val emailField = ObservableField<String>("")
	val passField = ObservableField<String>("")

	private val userEmail get() = emailField.get()!!
	private val userPass get() = passField.get()!!

	private val alertRequest = PublishSubject.create<Int>()
	private val signupRequest = PublishSubject.create<EmailAuthParams>()
	private val loginRequest = PublishSubject.create<Any>()

	private val credentials get() = composeParams()

	fun observeAlertRequest() = alertRequest as Observable<Int>
	fun observeSignupRequest() = signupRequest as Observable<EmailAuthParams>
	fun observeLoginRequest() = loginRequest as Observable<*>

	fun onSignupClicked() {
		signupRequest.onNext(credentials)
	}

	fun onLoginClicked() = loginRequest.onNext(EmptyObject)

	private fun composeParams() = EmailAuthParams(userEmail, userPass)
}
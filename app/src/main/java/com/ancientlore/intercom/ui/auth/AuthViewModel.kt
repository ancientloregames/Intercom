package com.ancientlore.intercom.ui.auth

import com.ancientlore.intercom.App
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class AuthViewModel : BasicViewModel() {

	protected val alertRequestSub = PublishSubject.create<Int>()

	val authManager get() = App.backend.getAuthManager()

	fun observeAlertRequest() = alertRequestSub as Observable<Int>
}
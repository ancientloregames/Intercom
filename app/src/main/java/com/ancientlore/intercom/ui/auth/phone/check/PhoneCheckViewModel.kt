package com.ancientlore.intercom.ui.auth.phone.check

import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.ui.auth.AuthViewModel
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PhoneCheckViewModel(private val params: PhoneAuthParams) : AuthViewModel() {

	companion object {
		const val TOAST_CODE_ERR = 0
	}

	val codeField = ObservableField("")

	private val openChatListSubj = PublishSubject.create<User>()

	fun onResendCodeClicked() {
		codeField.set("")
		App.backend.getAuthManager().loginViaPhone(params, object : AuthCallback {
			override fun onVerification(id: String) {
				params.validationId = id
			}
			override fun onSuccess(result: User) {
				openChatListSubj.onNext(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	fun onEnterClicked() {
		val code = codeField.get() ?: ""
		if (validateCode(code)) {
			App.backend.getAuthManager().verifySmsCode(code, params.validationId!!, object : CrashlyticsRequestCallback<User>() {
				override fun onSuccess(result: User) {
					openChatListSubj.onNext(result)
				}
				override fun onFailure(error: Throwable) {
					super.onFailure(error)
					if (authManager.isLoggedIn()) // FIXME walk around if device support auto verification
						openChatListSubj.onNext(authManager.getCurrentUser())
					else
						toastRequest.onNext(TOAST_CODE_ERR)
				}
			})
		}
	}

	fun openChatListRequest() = openChatListSubj as Observable<User>

	private fun validateCode(code: String): Boolean {
		return code.isNotBlank()
	}
}
package com.ancientlore.intercom.ui.auth.phone.login

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.BuildConfig
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.databinding.PhoneLoginUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginViewModel.Companion.ERROR_AUTH_FAILED
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginViewModel.Companion.ERROR_INVALID_PHONE
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginViewModel.Companion.ERROR_NO_PHONE
import java.lang.RuntimeException
import javax.inject.Inject

class PhoneLoginFragment
	: AuthFragment<PhoneLoginViewModel, PhoneLoginUiBinding>() {

	companion object {
		fun newInstance() = PhoneLoginFragment()
	}

	@Inject
	protected lateinit var viewModel: PhoneLoginViewModel

	override fun getAlertMessage(alertCode: Int): String {
		return when (alertCode) {
			ERROR_NO_PHONE -> getString(R.string.auth_alert_no_phone_msg)
			ERROR_INVALID_PHONE -> getString(R.string.auth_alert_invalid_phone_msg)
			ERROR_AUTH_FAILED -> getString(R.string.auth_failed_msg)
			else -> {
				if (BuildConfig.DEBUG)
					throw RuntimeException("Error! Unknown alert code: $alertCode")
				else getString(R.string.auth_alert_unknown_msg) + ": $alertCode"
			}
		}
	}

	override fun getLayoutResId() = R.layout.phone_login_ui

	override fun createDataBinding(view: View) = PhoneLoginUiBinding.bind(view)

	override fun requestViewModel() = PhoneLoginViewModel()

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.viewModel = viewModel

		subscriptions.add(viewModel.observeLoginSuccessEvent()
			.subscribe { onLoginSuccess(it) })
		subscriptions.add(viewModel.observeValidationRequestEvent()
			.subscribe { onValidationRequest(it) })
		subscriptions.add(viewModel.observeAlertRequest()
			.subscribe { showAlert(it) })
	}

	private fun onLoginSuccess(user: User) {
		navigator?.onSuccessfullAuth(user)
	}

	private fun onValidationRequest(phone: PhoneAuthParams) {
		navigator?.openPhoneCheckForm(phone)
	}
}
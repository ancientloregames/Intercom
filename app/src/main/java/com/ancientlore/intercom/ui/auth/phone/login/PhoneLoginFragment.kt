package com.ancientlore.intercom.ui.auth.phone.login

import android.view.View
import com.ancientlore.intercom.BuildConfig
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.databinding.PhoneLoginUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginViewModel.Companion.ERROR_INVALID_PHONE
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginViewModel.Companion.ERROR_NO_PHONE
import java.lang.RuntimeException

class PhoneLoginFragment
	: AuthFragment<PhoneLoginViewModel, PhoneLoginUiBinding>() {

	companion object {
		fun newInstance() = PhoneLoginFragment()
	}

	override fun getAlertMessage(alertCode: Int): String {
		return when (alertCode) {
			ERROR_NO_PHONE -> getString(R.string.auth_alert_no_phone_msg)
			ERROR_INVALID_PHONE -> getString(R.string.auth_alert_invalid_phone_msg)
			else -> {
				if (BuildConfig.DEBUG)
					throw RuntimeException("Error! Unknown alert code!")
				else getString(R.string.auth_alert_unknown_msg)
			}
		}
	}

	override fun getLayoutResId() = R.layout.phone_login_ui

	override fun createViewModel() = PhoneLoginViewModel()

	override fun bind(view: View, viewModel: PhoneLoginViewModel) {
		dataBinding = PhoneLoginUiBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: PhoneLoginViewModel) {
	}

	override fun observeViewModel(viewModel: PhoneLoginViewModel) {
		subscriptions.add(viewModel.observeLoginSuccessEvent()
			.subscribe { onLoginSuccess(it) })
		subscriptions.add(viewModel.observeValidationRequestEvent()
			.subscribe { onValidationRequest(it) })
		subscriptions.add(viewModel.observeAlertRequestEvent()
			.subscribe { showAlert(it) })
	}

	private fun onLoginSuccess(user: User) {
		navigator?.onSuccessfullAuth(user)
	}

	private fun onValidationRequest(phone: PhoneAuthParams) {
		navigator?.openPhoneCheckForm(phone)
	}
}
package com.ancientlore.intercom.ui.auth.email.login

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.databinding.EmailLoginUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.ui.auth.email.login.LoginViewModel.Companion.VALIDITY_EMPTY_FIELDS
import com.ancientlore.intercom.ui.auth.email.login.LoginViewModel.Companion.VALIDITY_EMPTY_ID
import com.ancientlore.intercom.ui.auth.email.login.LoginViewModel.Companion.VALIDITY_EMPTY_PASS

class LoginFragment : AuthFragment<LoginViewModel, EmailLoginUiBinding>() {

	companion object {
		fun newInstance() = LoginFragment()
	}

	override fun getLayoutResId() = R.layout.email_login_ui

	override fun createViewModel() = LoginViewModel()

	override fun bind(view: View, viewModel: LoginViewModel) {
		dataBinding = EmailLoginUiBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: LoginViewModel) {}

	override fun observeViewModel(viewModel: LoginViewModel) {
		subscriptions.add(viewModel.observeSignupRequest()
			.subscribe { openSignupForm() })

		subscriptions.add(viewModel.observeLoginRequest()
			.subscribe { params -> login(params) })

		subscriptions.add(viewModel.observeAlertRequest()
			.subscribe { alertCode -> showAlert(getAlertMessage(alertCode)) })
	}

	override fun getAlertMessage(alertCode: Int): String {
		return when (alertCode) {
			VALIDITY_EMPTY_FIELDS -> getString(R.string.auth_alert_no_creds_msg)
			VALIDITY_EMPTY_ID -> getString(R.string.auth_alert_no_login_msg)
			VALIDITY_EMPTY_PASS -> getString(R.string.auth_alert_no_pass_msg)
			else -> getString(R.string.auth_failure_msg)
		}
	}

	private fun openSignupForm() = navigator?.openSignupForm()

	private fun login(params: EmailAuthParams) {
		auth.login(params, object : AuthCallback {
			override fun onSuccess(result: AuthManager.User) = onSuccessfulAuth(result)
			override fun onFailure(error: Throwable) = onFailedAuth(error)
		})
	}
}
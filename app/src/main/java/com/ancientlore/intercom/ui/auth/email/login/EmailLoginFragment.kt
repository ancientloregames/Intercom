package com.ancientlore.intercom.ui.auth.email.login

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.databinding.EmailLoginUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginViewModel.Companion.ERROR_EMPTY_FIELDS
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginViewModel.Companion.ERROR_NO_EMAIL
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginViewModel.Companion.ERROR_NO_PASS
import java.lang.RuntimeException

class EmailLoginFragment : AuthFragment<EmailLoginViewModel, EmailLoginUiBinding>() {

	companion object {
		fun newInstance() = EmailLoginFragment()
	}

	override fun getLayoutResId() = R.layout.email_login_ui

	override fun createViewModel() = EmailLoginViewModel()

	override fun bind(view: View, viewModel: EmailLoginViewModel) {
		dataBinding = EmailLoginUiBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: EmailLoginViewModel) {}

	override fun observeViewModel(viewModel: EmailLoginViewModel) {
		subscriptions.add(viewModel.observeSignupRequest()
			.subscribe { openSignupForm() })

		subscriptions.add(viewModel.observeLoginRequest()
			.subscribe { params -> login(params) })

		subscriptions.add(viewModel.observeAlertRequest()
			.subscribe { alertCode -> showAlert(alertCode) })
	}

	override fun getAlertMessage(alertCode: Int): String {
		return when (alertCode) {
			ERROR_EMPTY_FIELDS -> getString(R.string.auth_alert_no_creds_msg)
			ERROR_NO_EMAIL -> getString(R.string.auth_alert_no_login_msg)
			ERROR_NO_PASS -> getString(R.string.auth_alert_no_pass_msg)
			else -> throw RuntimeException("Error! Unknown alert code!")
		}
	}

	private fun openSignupForm() = navigator?.openSignupForm()

	private fun login(params: EmailAuthParams) {
		auth.loginViaEmail(params, object : AuthCallback {
			override fun onSuccess(result: User) = onSuccessfulAuth(result)
			override fun onFailure(error: Throwable) = onFailedAuth(error)
		})
	}
}
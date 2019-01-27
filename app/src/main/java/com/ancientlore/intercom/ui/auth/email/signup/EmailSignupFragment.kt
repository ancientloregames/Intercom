package com.ancientlore.intercom.ui.auth.email.signup

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.databinding.EmailSignupUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel.Companion.ERROR_EMPTY_FIELDS
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel.Companion.ERROR_NO_EMAIL
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel.Companion.ERROR_NO_PASS
import java.lang.RuntimeException

class EmailSignupFragment : AuthFragment<EmailSignupViewModel, EmailSignupUiBinding>() {

	companion object {
		fun newInstance() = EmailSignupFragment()
	}

	override fun getLayoutResId() = R.layout.email_signup_ui

	override fun createViewModel() = EmailSignupViewModel()

	override fun bind(view: View, viewModel: EmailSignupViewModel) {
		dataBinding = EmailSignupUiBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: EmailSignupViewModel) {}

	override fun observeViewModel(viewModel: EmailSignupViewModel) {
		subscriptions.add(viewModel.observeLoginRequest()
			.subscribe { openLoginForm() })

		subscriptions.add(viewModel.observeSignupRequest()
			.subscribe { params -> signup(params) })

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

	private fun openLoginForm() = navigator?.openLoginForm()

	private fun signup(params: EmailAuthParams) {
		auth.signupViaEmail(params, object : AuthCallback {
			override fun onSuccess(result: User) = onSuccessfulAuth(result)
			override fun onFailure(error: Throwable) = onFailedAuth(error)
		})
	}
}
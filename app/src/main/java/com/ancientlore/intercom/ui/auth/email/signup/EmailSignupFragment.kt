package com.ancientlore.intercom.ui.auth.email.signup

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.databinding.EmailSignupUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel.Companion.ERROR_NO_EMAIL
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel.Companion.ERROR_NO_NAME
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel.Companion.ERROR_NO_PASS
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel.Companion.ERROR_PASS2
import java.lang.RuntimeException

class EmailSignupFragment : AuthFragment<EmailSignupViewModel, EmailSignupUiBinding>() {

	companion object {
		fun newInstance() = EmailSignupFragment()
	}

	override fun getLayoutResId() = R.layout.email_signup_ui

	override fun createDataBinding(view: View) = EmailSignupUiBinding.bind(view)

	override fun createViewModel() = EmailSignupViewModel()

	override fun init(viewModel: EmailSignupViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.viewModel = viewModel

		subscriptions.add(viewModel.observeLoginRequest()
			.subscribe { openLoginForm() })

		subscriptions.add(viewModel.observeSignupRequest()
			.subscribe { params -> signup(params) })

		subscriptions.add(viewModel.observeAlertRequest()
			.subscribe { alertCode -> showAlert(alertCode) })
	}

	override fun getAlertMessage(alertCode: Int): String {
		return when (alertCode) {
			ERROR_NO_NAME -> getString(R.string.auth_alert_no_name_msg)
			ERROR_NO_EMAIL -> getString(R.string.auth_alert_no_login_msg)
			ERROR_NO_PASS -> getString(R.string.auth_alert_no_pass_msg)
			ERROR_PASS2 -> getString(R.string.auth_alert_pass2_not_match_msg)
			else -> throw RuntimeException("Error! Unknown alert code!")
		}
	}

	private fun openLoginForm() = navigator?.openLoginForm()

	private fun signup(params: EmailAuthParams) {
		auth.signupViaEmail(params, object : AuthCallback {
			override fun onVerification(id: String) {}
			override fun onSuccess(result: User) = onSuccessfulAuth(result)
			override fun onFailure(error: Throwable) = onFailedAuth(error)
		})
	}
}
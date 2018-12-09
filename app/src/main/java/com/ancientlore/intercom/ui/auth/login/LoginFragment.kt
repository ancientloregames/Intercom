package com.ancientlore.intercom.ui.auth.login

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.databinding.LoginUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment

class LoginFragment : AuthFragment<LoginViewModel, LoginUiBinding>() {

	companion object {
		fun newInstance() = LoginFragment()
	}

	override fun getLayoutResId() = R.layout.login_ui

	override fun createViewModel() = LoginViewModel()

	override fun bind(view: View, viewModel: LoginViewModel) {
		dataBinding = LoginUiBinding.bind(view)
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
		TODO()
	}

	private fun openSignupForm() = navigator?.openSignupForm()

	private fun login(params: EmailAuthParams) {
		auth.login(params, object : AuthCallback {
			override fun onSuccess(result: AuthManager.User) = onSuccessfulAuth(result)
			override fun onFailure(error: Throwable) = onFailedAuth(error)
		})
	}
}
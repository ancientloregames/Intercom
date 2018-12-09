package com.ancientlore.intercom.ui.auth.signup

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.AuthCallback
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.backend.auth.EmailAuthParams
import com.ancientlore.intercom.databinding.SignupUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment

class SignupFragment : AuthFragment<SignupViewModel, SignupUiBinding>() {

	companion object {
		fun newInstance() = SignupFragment()
	}

	override fun getLayoutResId() = R.layout.signup_ui

	override fun createViewModel() = SignupViewModel()

	override fun bind(view: View, viewModel: SignupViewModel) {
		dataBinding = SignupUiBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: SignupViewModel) {}

	override fun observeViewModel(viewModel: SignupViewModel) {
		subscriptions.add(viewModel.observeLoginRequest()
			.subscribe { openLoginForm() })

		subscriptions.add(viewModel.observeSignupRequest()
			.subscribe { params -> signup(params) })

		subscriptions.add(viewModel.observeAlertRequest()
			.subscribe { alertCode -> showAlert(getAlertMessage(alertCode)) })
	}

	override fun getAlertMessage(alertCode: Int): String {
		TODO()
	}

	private fun openLoginForm() = navigator?.openLoginForm()

	private fun signup(params: EmailAuthParams) {
		auth.signup(params, object : AuthCallback {
			override fun onSuccess(result: AuthManager.User) = onSuccessfulAuth(result)
			override fun onFailure(error: Throwable) = onFailedAuth(error)
		})
	}
}
package com.ancientlore.intercom.ui.auth.phone.login

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.PhoneLoginUiBinding
import com.ancientlore.intercom.ui.auth.AuthFragment

class PhoneLoginFragment
	: AuthFragment<PhoneLoginViewModel, PhoneLoginUiBinding>() {

	companion object {
		fun newInstance() = PhoneLoginFragment()
	}

	override fun getAlertMessage(alertCode: Int): String {
		TODO()
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
		subscriptions.add(viewModel.observeEnterClickedEvent()
			.subscribe { onPhoneNumberEntered(it) })
	}

	private fun onPhoneNumberEntered(number: String) {
	}
}
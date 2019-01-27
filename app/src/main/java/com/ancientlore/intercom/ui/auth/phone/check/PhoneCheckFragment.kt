package com.ancientlore.intercom.ui.auth.phone.check

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.databinding.PhoneCheckUiBinding
import java.lang.RuntimeException

class PhoneCheckFragment
	: AuthFragment<PhoneCheckViewModel, PhoneCheckUiBinding>() {

	companion object {
		private const val ARG_PARAMS = "params"

		fun newInstance(params: PhoneAuthParams) : PhoneCheckFragment {
			return PhoneCheckFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params get() = arguments?.getParcelable<PhoneAuthParams>(ARG_PARAMS)
		?: throw RuntimeException("Phone number is a mandotory arg")

	override fun getAlertMessage(alertCode: Int): String {
		TODO()
	}

	override fun getLayoutResId() = R.layout.phone_check_ui

	override fun createViewModel() = PhoneCheckViewModel()

	override fun bind(view: View, viewModel: PhoneCheckViewModel) {
		dataBinding = PhoneCheckUiBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: PhoneCheckViewModel) {
		auth?.loginViaPhone(params, object : RequestCallback<User> {
			override fun onSuccess(result: User) {
				onSuccessfulAuth(result)
			}
			override fun onFailure(error: Throwable) {
				runOnUiThread(Runnable {
					Toast.makeText(context, R.string.verification_failure_msg, LENGTH_LONG).show()
				})
			}
		})
	}

	override fun observeViewModel(viewModel: PhoneCheckViewModel) {
		subscriptions.add(viewModel.observeEnterRequest()
			.subscribe { verifyCode(it) })
	}

	private fun verifyCode(code: String) {
	}
}
package com.ancientlore.intercom.ui.auth.phone.check

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.data.model.User
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
		// TODO
		return ""
	}

	override fun getLayoutResId() = R.layout.phone_check_ui

	override fun createDataBinding(view: View) = PhoneCheckUiBinding.bind(view)

	override fun createViewModel() = PhoneCheckViewModel(params)

	override fun init(viewModel: PhoneCheckViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.viewModel = viewModel

		subscriptions.addAll(
			viewModel.openChatListRequest()
				.subscribe {
					onSuccessfulAuth(it)
				},
				viewModel.observeToastRequest()
				.subscribe {

				}
		)
	}

	override fun getToastStringRes(toastId: Int): Int {
		return when (toastId) {
			PhoneCheckViewModel.TOAST_CODE_ERR ->R.string.verification_failure_msg
			else -> super.getToastStringRes(toastId)
		}
	}
}
package com.ancientlore.intercom.ui.auth.phone.check

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.ui.auth.AuthFragment
import com.ancientlore.intercom.databinding.PhoneCheckUiBinding
import javax.inject.Inject

class PhoneCheckFragment
	: AuthFragment<PhoneCheckViewModel, PhoneCheckUiBinding>() {

	companion object {

		fun newInstance(params: PhoneAuthParams) : PhoneCheckFragment {
			return PhoneCheckFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_FRAGMENT_PARAMS, params)
				}
			}
		}
	}

	@Inject
	protected lateinit var params: PhoneAuthParams

	@Inject
	protected lateinit var viewModel: PhoneCheckViewModel

	override fun getAlertMessage(alertCode: Int): String {
		// TODO
		return ""
	}

	override fun getLayoutResId() = R.layout.phone_check_ui

	override fun createDataBinding(view: View) = PhoneCheckUiBinding.bind(view)

	override fun requestViewModel(): PhoneCheckViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

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
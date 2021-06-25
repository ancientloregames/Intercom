package com.ancientlore.intercom.ui.call.offer

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.databinding.CallOfferUiBinding
import com.ancientlore.intercom.ui.call.CallFragment
import kotlinx.android.synthetic.main.call_offer_ui.*
import java.lang.RuntimeException

class CallOfferFragment : CallFragment<CallOfferViewModel, CallOfferUiBinding>() {

	companion object {

		const val ARG_TARGET_ID = "targetId"

		fun newInstance(targetId: String) : CallOfferFragment {
			return CallOfferFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_TARGET_ID, targetId)
				}
			}
		}
	}

	private val targetId : String by lazy { arguments?.getString(ARG_TARGET_ID)
		?: throw RuntimeException("Callee id is a mandotory arg") }

	override fun getLayoutResId() = R.layout.call_offer_ui

	override fun createViewModel() = CallOfferViewModel(targetId)

	override fun bind(view: View, viewModel: CallOfferViewModel) {
		dataBinding = CallOfferUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: CallOfferViewModel) {
		super.initViewModel(viewModel)

		App.backend.getCallManager().apply {
			setCallConnectionListener(viewModel)
			call(CallManager.CallParams(
				targetId,
				localVideoRenderer,
				remoteVideoRenderer
			))
		}
	}
}
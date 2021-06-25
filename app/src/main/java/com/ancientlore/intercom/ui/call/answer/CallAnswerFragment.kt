package com.ancientlore.intercom.ui.call.answer

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.CallManager
import com.ancientlore.intercom.data.model.call.Offer
import com.ancientlore.intercom.databinding.CallAnswerUiBinding
import com.ancientlore.intercom.ui.call.CallFragment
import kotlinx.android.synthetic.main.call_answer_ui.*
import java.lang.RuntimeException

// TODO maybe better to show local stream as main before actual answer?
class CallAnswerFragment : CallFragment<CallAnswerViewModel, CallAnswerUiBinding>() {

	companion object {

		const val ARG_CALLER_ID = "callerId"
		const val ARG_SDP = "sdp"

		fun newInstance(offer: Offer) : CallAnswerFragment {
			return CallAnswerFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_CALLER_ID, offer.callerId)
					putString(ARG_SDP, offer.sdp)
				}
			}
		}
	}

	private val callerId : String by lazy { arguments?.getString(ARG_CALLER_ID)
		?: throw RuntimeException("Caller id is a mandotory arg") }

	private val sdp : String by lazy { arguments?.getString(ARG_SDP)
		?: throw RuntimeException("sdp is a mandotory arg") }

	override fun getLayoutResId() = R.layout.call_answer_ui

	override fun createViewModel() = CallAnswerViewModel(callerId)

	override fun bind(view: View, viewModel: CallAnswerViewModel) {
		dataBinding = CallAnswerUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: CallAnswerViewModel) {
		super.initViewModel(viewModel)

		subscriptions.add(viewModel.observeAnswerCall()
			.subscribe {
				App.backend.getCallManager().apply {
					setCallConnectionListener(viewModel)
					answer(CallManager.CallParams(
						callerId,
						localVideoRenderer,
						remoteVideoRenderer), sdp)
				}
			})
		subscriptions.add(viewModel.observeDeclineCall()
			.subscribe {
				App.backend.getCallManager().hungup()
				close()
			})
	}
}
package com.ancientlore.intercom.ui.call.answer

import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.R
import com.ancientlore.intercom.ui.call.CallFragment

// TODO maybe better to show local stream as main before actual answer?
abstract class CallAnswerFragment<VM : CallAnswerViewModel, B : ViewDataBinding> : CallFragment<VM, B>() {

	abstract fun answer()

	override fun getCallSound(): Int = R.raw.incoming_call

	@CallSuper
	override fun initViewModel(viewModel: VM) {
		super.initViewModel(viewModel)

		subscriptions.add(viewModel.answerCallRequest()
			.subscribe {
				stopCallSound()
				answer()
			})
		subscriptions.add(viewModel.declineCallRequest()
			.subscribe {
				endCall()
			})
	}
}
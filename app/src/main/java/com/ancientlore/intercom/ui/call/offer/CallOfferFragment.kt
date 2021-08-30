package com.ancientlore.intercom.ui.call.offer

import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.R
import com.ancientlore.intercom.ui.call.CallFragment
abstract class CallOfferFragment<VM : CallOfferViewModel, B : ViewDataBinding>
	: CallFragment<VM, B>() {

	override fun getCallSound(): Int = R.raw.outgoing_call

	override fun initViewModel(viewModel: VM) {
		super.initViewModel(viewModel)

		subscriptions.add(viewModel.stopCallSoundRequest()
			.subscribe { stopCallSound() })
	}
}
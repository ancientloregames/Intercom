package com.ancientlore.intercom.ui.call.offer

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.R
import com.ancientlore.intercom.ui.call.CallFragment
abstract class CallOfferFragment<VM : CallOfferViewModel, B : ViewDataBinding>
	: CallFragment<VM, B>() {

	override fun getCallSound(): Int = R.raw.outgoing_call

	override fun init(viewModel: VM, savedState: Bundle?) {
		super.init(viewModel, savedState)

		subscriptions.add(viewModel.stopCallSoundRequest()
			.subscribe { stopCallSound() })
	}
}
package com.ancientlore.intercom.ui.call

import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.App
import com.ancientlore.intercom.ui.BasicFragment

abstract class CallFragment<VM : CallViewModel, B : ViewDataBinding>  : BasicFragment<VM, B>() {

	protected val userId = App.backend.getAuthManager().getCurrentUser().id

	override fun onBackPressed(): Boolean {
		App.backend.getCallManager().hungup()
		close()
		return true
	}

	override fun initViewModel(viewModel: VM) {

		subscriptions.add(viewModel.observeHangupCall()
			.subscribe {
				App.backend.getCallManager().hungup()
				close()
			})
	}
}
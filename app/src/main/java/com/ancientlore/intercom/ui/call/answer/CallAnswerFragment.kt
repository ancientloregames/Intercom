package com.ancientlore.intercom.ui.call.answer

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.R
import com.ancientlore.intercom.ui.call.CallFragment

// TODO maybe better to show local stream as main before actual answer?
abstract class CallAnswerFragment<VM : CallAnswerViewModel, B : ViewDataBinding> : CallFragment<VM, B>() {

	override fun getCallSound(): Int = R.raw.incoming_call

	@CallSuper
	override fun init(savedState: Bundle?) {
		super.init(savedState)

		subscriptions.add(requestViewModel().stopCallSoundRequest()
			.subscribe {
				stopCallSound()
			})
		subscriptions.add(requestViewModel().closeRequest()
			.subscribe {
				endCall()
			})
	}
}
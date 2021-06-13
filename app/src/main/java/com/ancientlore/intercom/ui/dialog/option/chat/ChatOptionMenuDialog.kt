package com.ancientlore.intercom.ui.dialog.option.chat

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatOptionMenuUiBinding
import com.ancientlore.intercom.ui.dialog.option.BasicOptionMenuDialog

class ChatOptionMenuDialog: BasicOptionMenuDialog<ChatOptionMenuViewModel>() {

	companion object {
		const val ARG_PARAMS = "arg_params"

		fun newInstance(params: ChatOptionMenuParams): ChatOptionMenuDialog {
			return ChatOptionMenuDialog().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : ChatOptionMenuParams by lazy {
		arguments?.getParcelable<ChatOptionMenuParams>(ARG_PARAMS)
		?: throw RuntimeException("Params are a mandotory arg") }

	interface Listener {
		fun onPinClicked(pin: Boolean)
		fun onMuteClicked(pin: Boolean)
	}
	var listener: Listener? = null

	override fun getLayoutResId() = R.layout.chat_option_menu_ui

	override fun setupViewModel(view: View) {
		val binder = ChatOptionMenuUiBinding.bind(view)
		viewModel = ChatOptionMenuViewModel(params)
		binder.ui = viewModel
	}

	override fun onDestroyView() {
		listener = null

		super.onDestroyView()
	}

	override fun subscribeOnViewModel() {
		subscriptions.add(viewModel.observePinClicked()
			.subscribe {
				listener?.onPinClicked(it)
				dismiss()
			})

		subscriptions.add(viewModel.observeMuteClicked()
			.subscribe {
				listener?.onMuteClicked(it)
				dismiss()
			})
	}
}
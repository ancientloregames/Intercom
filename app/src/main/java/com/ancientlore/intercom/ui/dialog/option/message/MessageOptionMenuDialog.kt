package com.ancientlore.intercom.ui.dialog.option.message

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.MessageOptionMenuUiBinding
import com.ancientlore.intercom.ui.dialog.option.BasicOptionMenuDialog

class MessageOptionMenuDialog: BasicOptionMenuDialog<MessageOptionMenuViewModel>() {

	companion object {

		fun newInstance(): MessageOptionMenuDialog {
			return MessageOptionMenuDialog()
		}
	}

	interface Listener {
		fun onDeleteClicked()
	}
	var listener: Listener? = null

	override fun getLayoutResId() = R.layout.message_option_menu_ui

	override fun setupViewModel(view: View) {
		val binder = MessageOptionMenuUiBinding.bind(view)
		viewModel = MessageOptionMenuViewModel()
		binder.ui = viewModel
	}

	override fun onDestroyView() {
		listener = null

		super.onDestroyView()
	}

	override fun subscribeOnViewModel() {
		subscriptions.add(viewModel.observeDeleteClicked()
			.subscribe {
				listener?.onDeleteClicked()
				dismiss()
			})
	}
}
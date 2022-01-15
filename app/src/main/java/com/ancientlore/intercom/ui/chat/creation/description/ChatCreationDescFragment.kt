package com.ancientlore.intercom.ui.chat.creation.description

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ChatCreationDescUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.showKeyboard
import javax.inject.Inject

class ChatCreationDescFragment
	: FilterableFragment<ChatCreationDescViewModel, ChatCreationDescUiBinding>() {

	companion object {
		const val INTENT_GET_IMAGE = 101

		const val ARG_PARAMS = "params"

		fun newInstance(contacts: List<Contact>) : ChatCreationDescFragment {
			return ChatCreationDescFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS,
						ChatCreationDescViewModel.Params(
							if (contacts is ArrayList) contacts else ArrayList(contacts)
						))
				}
			}
		}
	}

	@Inject
	lateinit var params: ChatCreationDescViewModel.Params

	@Inject
	protected lateinit var viewModel: ChatCreationDescViewModel

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId() = R.menu.chat_creation_desc_menu

	override fun getLayoutResId() = R.layout.chat_creation_desc_ui

	override fun createDataBinding(view: View) = ChatCreationDescUiBinding.bind(view)

	override fun requestViewModel(): ChatCreationDescViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
			setSubtitle(getString(R.string.member_count, params.contacts.size))
		}

		dataBinding.swipableLayout.setListener { close(false) }

		dataBinding.listView.adapter = viewModel.getListAdapter()

		subscriptions.add(viewModel.observeCreateChatRequest()
			.subscribe {
				navigator?.openChatFlow(it)
			})
		subscriptions.add(viewModel.observeOpenGallaryRequest()
			.subscribe {
				navigator?.openImagePicker(this, INTENT_GET_IMAGE)
			})

		Utils.runOnUiThread({ dataBinding.nameView.showKeyboard() }, 200)
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

		when (requestCode) {
			INTENT_GET_IMAGE -> {
				if (resultCode == RESULT_OK && intent != null) {
					if (intent.data != null && intent.data != Uri.EMPTY) {
						viewModel.onChatIconSelected(intent.data)
					}
					else {
						Utils.logError("ChatCreationDescFragment.handleGetImageIntent(): No data in the intent")
						showToast(R.string.alert_error_set_photo)
					}
				}
			}
			else -> super.onActivityResult(requestCode, resultCode, intent)
		}
	}

	override fun getToastStringRes(toastId: Int): Int {
		return when (toastId) {
			ChatCreationDescViewModel.TOAST_REQUIRED_NAME_ERR -> R.string.alert_error_name_required
			else -> super.getToastStringRes(toastId)
		}
	}
}

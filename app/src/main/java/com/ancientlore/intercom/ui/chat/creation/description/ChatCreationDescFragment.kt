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
import java.lang.RuntimeException

class ChatCreationDescFragment : FilterableFragment<ChatCreationDescViewModel, ChatCreationDescUiBinding>() {

	companion object {
		const val INTENT_GET_IMAGE = 101

		private const val ARG_CONTACTS = "contacts"

		fun newInstance(contacts: List<Contact>) : ChatCreationDescFragment {
			return ChatCreationDescFragment().apply {
				arguments = Bundle().apply {
					putParcelableArrayList(ARG_CONTACTS,
						if (contacts is ArrayList) contacts else ArrayList(contacts))
				}
			}
		}
	}

	private val contacts: List<Contact> by lazy { arguments?.getParcelableArrayList(ARG_CONTACTS)
		?: throw RuntimeException("Contacts list is a mandotory arg") }

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId() = R.menu.chat_creation_desc_menu

	override fun getLayoutResId() = R.layout.chat_creation_desc_ui

	override fun createDataBinding(view: View) = ChatCreationDescUiBinding.bind(view)

	override fun createViewModel() =
		ChatCreationDescViewModel(
			ChatCreationDescAdapter(requireContext()))

	override fun init(viewModel: ChatCreationDescViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
			setSubtitle(getString(R.string.member_count, contacts.size))
		}

		dataBinding.swipableLayout.setListener { close(false) }

		dataBinding.listView.adapter = viewModel.listAdapter

		viewModel.init(contacts)

		subscriptions.add(viewModel.observeCreateChatRequest()
			.subscribe {
				navigator?.openChatFlow(it)
			})
		subscriptions.add(viewModel.observeOpenGallaryRequest()
			.subscribe {
				openGallery()
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

	private fun openGallery() {
		permissionManager?.requestPermissionWriteStorage { granted ->
			if (granted) {
				// FIXME temporary solution (TODO make own gallery)
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("image/*")
				startActivityForResult(intent, INTENT_GET_IMAGE)
			}
		}
	}
}

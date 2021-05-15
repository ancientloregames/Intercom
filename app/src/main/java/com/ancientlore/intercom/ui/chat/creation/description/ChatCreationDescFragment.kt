package com.ancientlore.intercom.ui.chat.creation.description

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.C
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ChatCreationDescUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.getAppCacheDir
import com.ancientlore.intercom.utils.extensions.getFileData
import kotlinx.android.synthetic.main.chat_creation_desc_ui.listView
import kotlinx.android.synthetic.main.chat_creation_desc_ui.swipableLayout
import kotlinx.android.synthetic.main.chat_creation_desc_ui.toolbar
import java.io.File
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

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	private val contacts: List<Contact> by lazy { arguments?.getParcelableArrayList(ARG_CONTACTS)
		?: throw RuntimeException("Contacts list is a mandotory arg") }

	override fun getToolbar(): Toolbar = toolbar

	override fun getToolbarMenuResId() = R.menu.chat_creation_desc_menu

	override fun getLayoutResId() = R.layout.chat_creation_desc_ui

	override fun createViewModel() = ChatCreationDescViewModel(listView.adapter as ChatCreationDescAdapter)

	override fun bind(view: View, viewModel: ChatCreationDescViewModel) {
		dataBinding = ChatCreationDescUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton { close() }
		}

		swipableLayout.setListener { close() }

		listView.adapter = ChatCreationDescAdapter(requireContext())
	}

	override fun initViewModel(viewModel: ChatCreationDescViewModel) {
		viewModel.init(contacts)
	}

	override fun observeViewModel(viewModel: ChatCreationDescViewModel) {
		super.observeViewModel(viewModel)

		subscriptions.add(viewModel.observeCreateChatRequest()
			.subscribe {
				navigator?.openChatFlow(it)
			})
		subscriptions.add(viewModel.observeOpenGallaryRequest()
			.subscribe {
				openGallery()
			})
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

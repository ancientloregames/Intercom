package com.ancientlore.intercom.ui.chat.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatDetailUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescAdapter
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import kotlinx.android.synthetic.main.chat_detail_ui.*

class ChatDetailFragment : FilterableFragment<ChatDetailViewModel, ChatDetailUiBinding>() {

	companion object {
		const val INTENT_GET_IMAGE = 101

		private const val ARG_PARAMS = "params"

		fun newInstance(params: ChatFlowParams) : ChatDetailFragment {
			return ChatDetailFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_PARAMS, params)
				}
			}
		}
	}

	private val params : ChatFlowParams by lazy { arguments?.getParcelable<ChatFlowParams>(ARG_PARAMS)
		?: throw RuntimeException("Chat params are a mandotory arg") }

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getToolbar(): Toolbar = toolbar

	override fun getLayoutResId(): Int = R.layout.chat_detail_ui

	override fun getToolbarMenuResId() = R.menu.chat_creation_desc_menu

	override fun createViewModel() = ChatDetailViewModel(listView.adapter as ChatCreationDescAdapter, params)

	override fun bind(view: View, viewModel: ChatDetailViewModel) {
		dataBinding = ChatDetailUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton { close() }
			setSubtitle(getString(R.string.member_count, params.participants.size))
		}

		swipableLayout.setListener { close() }

		listView.adapter = ChatCreationDescAdapter(requireContext())
	}

	override fun initViewModel(viewModel: ChatDetailViewModel) {
		subscriptions.add(viewModel.observeOpenGallaryRequest()
			.subscribe {
				openGallery()
			})
		subscriptions.add(viewModel.observeCloseRequest()
			.subscribe {
				close()
			})
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

		when (requestCode) {
			ChatCreationDescFragment.INTENT_GET_IMAGE -> {
				if (resultCode == Activity.RESULT_OK && intent != null) {
					if (intent.data != null && intent.data != Uri.EMPTY) {
						viewModel.onChatIconSelected(intent.data)
					}
					else {
						Utils.logError("ChatDetail.handleGetImageIntent(): No data in the intent")
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
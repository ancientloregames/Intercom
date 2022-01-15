package com.ancientlore.intercom.ui.chat.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatDetailUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescFragment
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import javax.inject.Inject

class ChatDetailFragment
	: FilterableFragment<ChatDetailViewModel, ChatDetailUiBinding>() {

	companion object {
		const val INTENT_GET_IMAGE = 101

		fun newInstance(params: ChatDetailViewModel.Params) : ChatDetailFragment {
			return ChatDetailFragment().apply {
				arguments = Bundle().apply {
					putParcelable(ARG_FRAGMENT_PARAMS, params)
				}
			}
		}
	}

	protected lateinit var params: ChatDetailViewModel.Params

	@Inject
	protected lateinit var viewModel: ChatDetailViewModel

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getLayoutResId(): Int = R.layout.chat_detail_ui

	override fun getToolbarMenuResId() = R.menu.chat_creation_desc_menu

	override fun createDataBinding(view: View) = ChatDetailUiBinding.bind(view)

	override fun requestViewModel(): ChatDetailViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
			setSubtitle(getString(R.string.member_count, params.participants.size))
		}

		dataBinding.swipableLayout.setListener { close(false) }

		dataBinding.listView.adapter = viewModel.getListAdapter()

		subscriptions.add(viewModel.observeOpenGallaryRequest()
			.subscribe {
				navigator?.openImagePicker(this, INTENT_GET_IMAGE)
			})
		subscriptions.add(viewModel.openImageViewerRequest()
			.subscribe {
				navigator?.openImageViewer(it)
			})
		subscriptions.add(viewModel.observeCloseRequest()
			.subscribe {
				close()
			})
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
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

	override fun getToastStringRes(toastId: Int): Int {
		return when (toastId) {
			ChatDetailViewModel.TOAST_SET_PHOTO_ERR -> R.string.alert_error_set_photo
			else -> super.getToastStringRes(toastId)
		}
	}
}
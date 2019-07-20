package com.ancientlore.intercom.ui.chat.flow

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatFlowUiBinding
import com.ancientlore.intercom.dialog.bottomsheet.list.ListBottomSheetDialog
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.ui.dialog.attach.AttachBottomSheetDialog
import com.ancientlore.intercom.utils.Runnable1
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.extensions.enableChatBehavior
import com.ancientlore.intercom.utils.extensions.getFileData
import com.ancientlore.intercom.widget.list.simple.SimpleListItem
import kotlinx.android.synthetic.main.chat_flow_ui.*

class ChatFlowFragment : BasicFragment<ChatFlowViewModel, ChatFlowUiBinding>() {

	companion object {
		const val INTENT_GET_CONTENT = 101

		private const val ARG_CHAT_ID = "chat_id"
		private const val ARG_CHAT_TITLE = "chat_title"

		fun newInstance(params: Params) : ChatFlowFragment {
			return ChatFlowFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_CHAT_ID, params.chatId)
					putString(ARG_CHAT_TITLE, params.title)
				}
			}
		}
	}

	data class Params(val chatId: String, val title: String)

	private val chatId get() = arguments?.getString(ARG_CHAT_ID)
		?: throw RuntimeException("Chat id is a mandotory arg")

	private val title get() = arguments?.getString(ARG_CHAT_TITLE)
		?: throw RuntimeException("Chat title is a mandotory arg")

	private val userId get() = App.backend.getAuthManager().getCurrentUser()?.id
		?: throw RuntimeException("This fragment may be created only after successful authorization")

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getLayoutResId() = R.layout.chat_flow_ui

	override fun createViewModel() = ChatFlowViewModel(userId, chatId)

	override fun bind(view: View, viewModel: ChatFlowViewModel) {
		dataBinding = ChatFlowUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		ToolbarManager(toolbar as Toolbar).apply {
			setTitle(title)
			enableBackButton(View.OnClickListener {
				close()
			})
		}

		swipableLayout.setListener { close() }

		with(listView) {
			adapter = ChatFlowAdapter(userId, context!!, mutableListOf())
			enableChatBehavior()
		}
	}

	override fun initViewModel(viewModel: ChatFlowViewModel) {
		viewModel.setListAdapter(listView.adapter as ChatFlowAdapter)
	}

	override fun observeViewModel(viewModel: ChatFlowViewModel) {
		subscriptions.add(viewModel.observeToastRequest()
			.subscribe { showToast(it) })
		subscriptions.add(viewModel.observeAttachMenuOpen()
			.subscribe { openAttachMenu() })
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		data?.let { result ->
			when (requestCode) {
				INTENT_GET_CONTENT -> {
					when (resultCode) {
						RESULT_OK -> {
							val uri = getIntentResult(result)
							val localFile = uri.getFileData(context!!.contentResolver)
							viewModel.handleAttachedFile(localFile)
						}
						else -> showToast(R.string.alert_error_attach_file)
					}
				}
				else -> super.onActivityResult(requestCode, resultCode, data)
			}
		}
	}

	private fun getIntentResult(intent: Intent) : Uri {
		// TODO multiple selection case (clipData)
		return intent.data ?: Uri.EMPTY
	}

	private fun openAttachMenu() {
		AttachBottomSheetDialog.newInstance().apply {
			setListener(object : ListBottomSheetDialog.Listener {
				override fun onItemSelected(item: SimpleListItem) {
					onAttachMenuItemSelected(item.id)
				}
			})
		}.show(fragmentManager!!)
	}

	private fun onAttachMenuItemSelected(id: Int) {
		when (id) {
			R.id.im_attach_file -> openFilePicker()
			else -> throw RuntimeException("Error! Unknown attach source")
		}
	}

	private fun openFilePicker() {
		permissionManager?.requestPermissionReadStorage(Runnable1 { granted ->
			if (granted) {
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("*/*")
				// TODO multiple selection .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
				startActivityForResult(intent, INTENT_GET_CONTENT)
			}
		})
	}
}

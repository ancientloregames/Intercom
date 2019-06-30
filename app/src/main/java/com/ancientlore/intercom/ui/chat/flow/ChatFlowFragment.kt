package com.ancientlore.intercom.ui.chat.flow

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatFlowUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.extensions.enableChatBehavior
import kotlinx.android.synthetic.main.chat_flow_ui.*

class ChatFlowFragment : BasicFragment<ChatFlowViewModel, ChatFlowUiBinding>() {

	companion object {
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
	}
}

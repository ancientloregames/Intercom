package com.ancientlore.intercom.ui.chat.detail

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatDetailUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.extensions.enableChatBehavior
import kotlinx.android.synthetic.main.chat_detail_ui.*

class ChatDetailFragment : BasicFragment<ChatDetailViewModel, ChatDetailUiBinding>() {

	companion object {
		private const val ARG_CHAT_ID = "chat_id"

		fun newInstance(chatId: String) : ChatDetailFragment {
			return ChatDetailFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_CHAT_ID, chatId)
				}
			}
		}
	}

	private val chatId get() = arguments?.getString(ARG_CHAT_ID)
		?: throw RuntimeException("Chat id is a mandotory arg")

	private val userId get() = App.backend.getAuthManager().getCurrentUser()?.id
		?: throw RuntimeException("This fragment may be created only after successful authorization")

	override fun getLayoutResId() = R.layout.chat_detail_ui

	override fun createViewModel() = ChatDetailViewModel(userId, chatId)

	override fun bind(view: View, viewModel: ChatDetailViewModel) {
		dataBinding = ChatDetailUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: ChatDetailViewModel) {
		val listAdapter = ChatDetailAdapter(userId, context!!, mutableListOf())
		listView.adapter = listAdapter
		listView.enableChatBehavior()
		viewModel.setListAdapter(listAdapter)
	}

	override fun observeViewModel(viewModel: ChatDetailViewModel) {
	}
}

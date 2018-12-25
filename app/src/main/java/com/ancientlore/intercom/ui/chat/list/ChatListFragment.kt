package com.ancientlore.intercom.ui.chat.list

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatListUiBinding
import com.ancientlore.intercom.ui.BasicFragment

class ChatListFragment : BasicFragment<ChatListViewModel, ChatListUiBinding>() {

	companion object {
		fun newInstance() = ChatListFragment()
	}

	override fun getLayoutResId() = R.layout.chat_list_ui

	override fun createViewModel() = ChatListViewModel()

	override fun bind(view: View, viewModel: ChatListViewModel) {
		dataBinding = ChatListUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: ChatListViewModel) {}

	override fun observeViewModel(viewModel: ChatListViewModel) {}
}

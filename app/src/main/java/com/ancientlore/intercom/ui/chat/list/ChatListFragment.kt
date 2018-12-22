package com.ancientlore.intercom.ui.chat.list

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatsListFragmentBinding
import com.ancientlore.intercom.ui.BasicFragment

class ChatListFragment : BasicFragment<ChatListViewModel, ChatsListFragmentBinding>() {

	companion object {
		fun newInstance() = ChatListFragment()
	}

	override fun getLayoutResId() = R.layout.chat_list_fragment

	override fun createViewModel() = ChatListViewModel()

	override fun bind(view: View, viewModel: ChatListViewModel) {
		dataBinding = ChatsListFragmentBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: ChatListViewModel) {}

	override fun observeViewModel(viewModel: ChatListViewModel) {}
}

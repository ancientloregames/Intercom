package com.ancientlore.intercom.ui.chatlist

import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatsListFragmentBinding
import com.ancientlore.intercom.ui.BasicFragment

class ChatListFragment : BasicFragment<ChatsListViewModel, ChatsListFragmentBinding>() {

	companion object {
		fun newInstance() = ChatListFragment()
	}

	override fun getLayoutResId() = R.layout.chats_list_fragment

	override fun createViewModel() = ChatsListViewModel()

	override fun bind(view: View, viewModel: ChatsListViewModel) {
		dataBinding = ChatsListFragmentBinding.bind(view)
		dataBinding.viewModel = viewModel
	}

	override fun initViewModel(viewModel: ChatsListViewModel) {}

	override fun observeViewModel(viewModel: ChatsListViewModel) {}
}

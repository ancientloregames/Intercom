package com.ancientlore.intercom.ui.chat.list

import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatListUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import kotlinx.android.synthetic.main.chat_list_ui.*

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

	override fun initView(view: View, savedInstanceState: Bundle?) {
		listView.adapter = ChatListAdapter(context!!, mutableListOf())
	}

	override fun initViewModel(viewModel: ChatListViewModel) {
		viewModel.setListAdapter(listView.adapter as ChatListAdapter)
	}

	override fun observeViewModel(viewModel: ChatListViewModel) {
		subscriptions.add(viewModel.observeContactListRequest()
			.subscribe { openContactList() })
		subscriptions.add(viewModel.observeChatOpenById()
			.subscribe { openChat(it) })
	}

	private fun openContactList() = navigator?.openContactList()

	private fun openChat(id: String) = navigator?.openChatFlow(id)
}

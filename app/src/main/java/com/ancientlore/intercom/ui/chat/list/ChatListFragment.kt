package com.ancientlore.intercom.ui.chat.list

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

	override fun initViewModel(viewModel: ChatListViewModel) {
		val listAdapter = ChatListAdapter(context!!, mutableListOf())
		listView.adapter = listAdapter
		viewModel.setListAdapter(listAdapter)
	}

	override fun observeViewModel(viewModel: ChatListViewModel) {
		subscriptions.add(viewModel.observeContactListRequest()
			.subscribe { openContactList() })
		subscriptions.add(viewModel.observeChatOpenById()
			.subscribe { openChat(it) })
	}

	private fun openContactList() = navigator?.openContactList()

	private fun openChat(id: String) = navigator?.openChatDetail(id)
}

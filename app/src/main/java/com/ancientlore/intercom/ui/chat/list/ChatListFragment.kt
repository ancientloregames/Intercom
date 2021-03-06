package com.ancientlore.intercom.ui.chat.list

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatListUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.utils.Runnable1
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
		initToolbarMenu()
		listView.adapter = ChatListAdapter(context!!, mutableListOf())
	}

	private fun initToolbarMenu() {
		navigator?.createToolbarMenu(toolbar, Runnable1 { menu ->
			activity?.menuInflater?.inflate(R.menu.chat_list_menu, menu)
			val search = menu.findItem(R.id.search).actionView as SearchView
			search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
				override fun onQueryTextSubmit(query: String?): Boolean {
					query?.let { constraint ->
						viewModel.filter(constraint)
					}
					return true
				}
				override fun onQueryTextChange(newText: String?): Boolean {
					newText
						?.takeIf { it.length > 1 }
						?.let { viewModel.filter(it) }
						?:run { viewModel.filter("") }
					return true
				}
			})
		})
	}

	override fun initViewModel(viewModel: ChatListViewModel) {
		viewModel.setListAdapter(listView.adapter as ChatListAdapter)
	}

	override fun observeViewModel(viewModel: ChatListViewModel) {
		subscriptions.add(viewModel.observeContactListRequest()
			.subscribe { openContactList() })
		subscriptions.add(viewModel.observeChatOpen()
			.subscribe { openChatFlow(it) })
	}

	private fun openContactList() = navigator?.openContactList()

	private fun openChatFlow(params: ChatFlowFragment.Params) = navigator?.openChatFlow(params)
}

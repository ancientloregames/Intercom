package com.ancientlore.intercom.ui.chat.creation

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatCreationUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.ToolbarManager
import kotlinx.android.synthetic.main.chat_creation_ui.*

class ChatCreationFragment : BasicFragment<ChatCreationViewModel, ChatCreationUiBinding>() {

	companion object {
		fun newInstance() = ChatCreationFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getLayoutResId() = R.layout.chat_creation_ui

	override fun createViewModel() = ChatCreationViewModel()

	override fun bind(view: View, viewModel: ChatCreationViewModel) {
		dataBinding = ChatCreationUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		initToolbarMenu()
		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton { close() }
		}

		swipableLayout.setListener { close() }

		listView.adapter = ChatCreationAdapter(context!!, mutableListOf())
	}

	override fun initViewModel(viewModel: ChatCreationViewModel) {
		viewModel.init(listView.adapter as ChatCreationAdapter)
	}

	override fun observeViewModel(viewModel: ChatCreationViewModel) {
		super.observeViewModel(viewModel)

		subscriptions.add(viewModel.observeChatOpen()
			.subscribe {
				val nav = navigator
				close()
				nav?.openChatFlow(it)
			})

		subscriptions.add(viewModel.observeCreateGroup()
			.subscribe {
				val nav = navigator
				close()
				nav?.openChatFlow(it)
			})
	}

	private fun initToolbarMenu() {
		navigator?.createToolbarMenu(toolbar) { menu ->
			activity?.menuInflater?.inflate(R.menu.chat_flow_menu, menu)
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
						?: run { viewModel.filter("") }
					return true
				}
			})
		}
	}
}

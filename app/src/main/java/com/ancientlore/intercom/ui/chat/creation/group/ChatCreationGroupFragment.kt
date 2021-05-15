package com.ancientlore.intercom.ui.chat.creation.group

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatCreationGroupUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ToolbarManager
import kotlinx.android.synthetic.main.chat_creation_group_ui.*

class ChatCreationGroupFragment : FilterableFragment<ChatCreationGroupViewModel, ChatCreationGroupUiBinding>() {

	companion object {
		fun newInstance() = ChatCreationGroupFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getToolbar(): Toolbar = toolbar

	override fun getToolbarMenuResId() = R.menu.chat_creation_group_menu

	override fun getLayoutResId() = R.layout.chat_creation_group_ui

	override fun createViewModel() = ChatCreationGroupViewModel(listView.adapter as ChatCreationGroupAdapter)

	override fun bind(view: View, viewModel: ChatCreationGroupViewModel) {
		dataBinding = ChatCreationGroupUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton { close() }
		}

		swipableLayout.setListener { close() }

		listView.adapter = ChatCreationGroupAdapter(requireContext())
	}

	override fun initViewModel(viewModel: ChatCreationGroupViewModel) {
		viewModel.init()
	}

	override fun observeViewModel(viewModel: ChatCreationGroupViewModel) {
		super.observeViewModel(viewModel)

		subscriptions.add(viewModel.observeNextRequest()
			.subscribe {
				navigator?.openChatCreationDesc(it)
			})
	}
}

package com.ancientlore.intercom.ui.chat.creation.group

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatCreationGroupUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ToolbarManager
import javax.inject.Inject

class ChatCreationGroupFragment
	: FilterableFragment<ChatCreationGroupViewModel, ChatCreationGroupUiBinding>() {

	companion object {
		fun newInstance() = ChatCreationGroupFragment()
	}

	@Inject
	protected lateinit var viewModel: ChatCreationGroupViewModel

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId() = R.menu.chat_creation_group_menu

	override fun getLayoutResId() = R.layout.chat_creation_group_ui

	override fun createDataBinding(view: View) = ChatCreationGroupUiBinding.bind(view)

	override fun requestViewModel(): ChatCreationGroupViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
		}

		dataBinding.swipableLayout.setListener { close(false) }

		dataBinding.listView.adapter = viewModel.getListAdapter()
		dataBinding.selectedListView.adapter = viewModel.selectedListAdapter

		viewModel.init()

		subscriptions.add(viewModel.observeNextRequest()
			.subscribe {
				navigator?.openChatCreationDesc(it)
			})
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}
}

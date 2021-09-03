package com.ancientlore.intercom.ui.chat.creation

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatCreationUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.utils.ToolbarManager
import kotlinx.android.synthetic.main.chat_creation_ui.*

class ChatCreationFragment : FilterableFragment<ChatCreationViewModel, ChatCreationUiBinding>() {

	companion object {
		fun newInstance() = ChatCreationFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getToolbar(): Toolbar = toolbar

	override fun getToolbarMenuResId() = R.menu.chat_creation_menu

	override fun getLayoutResId() = R.layout.chat_creation_ui

	override fun createViewModel() = ChatCreationViewModel(listView.adapter as ChatCreationAdapter)

	override fun bind(view: View, viewModel: ChatCreationViewModel) {
		dataBinding = ChatCreationUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		ToolbarManager(toolbar as Toolbar).apply {
			enableBackButton { close() }
		}

		swipableLayout.setListener { close(false) }

		listView.adapter = ChatCreationAdapter(requireContext())
	}

	override fun initViewModel(viewModel: ChatCreationViewModel) {
		viewModel.init()
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
				navigator?.openChatCreationGroup()
			})
		subscriptions.add(viewModel.observeAddContact()
			.subscribe {
				addContact()
			})
		subscriptions.add(viewModel.observeUpdateContactCount()
			.subscribe {
				runOnUiThread {
					(toolbar as Toolbar).subtitle = getString(R.string.contact_count, it)
				}
			})
	}

	private fun addContact() {
		val intent = Intent(Intent.ACTION_INSERT)
			.setType(ContactsContract.Contacts.CONTENT_TYPE)
			.putExtra("finishActivityOnSaveCompleted", true)
		startActivity(intent)
	}
}

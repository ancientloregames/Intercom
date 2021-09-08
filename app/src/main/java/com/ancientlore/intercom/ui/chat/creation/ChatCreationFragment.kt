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

class ChatCreationFragment : FilterableFragment<ChatCreationViewModel, ChatCreationUiBinding>() {

	companion object {
		fun newInstance() = ChatCreationFragment()
	}

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId() = R.menu.chat_creation_menu

	override fun getLayoutResId() = R.layout.chat_creation_ui

	override fun createDataBinding(view: View) = ChatCreationUiBinding.bind(view)

	override fun createViewModel() =
		ChatCreationViewModel(
			ChatCreationAdapter(requireContext()))

	override fun init(viewModel: ChatCreationViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
		}

		dataBinding.swipableLayout.setListener { close(false) }

		dataBinding.listView.adapter = viewModel.listAdapter

		viewModel.init()

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
					dataBinding.toolbar.subtitle = getString(R.string.contact_count, it)
				}
			})
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}

	private fun addContact() {
		val intent = Intent(Intent.ACTION_INSERT)
			.setType(ContactsContract.Contacts.CONTENT_TYPE)
			.putExtra("finishActivityOnSaveCompleted", true)
		startActivity(intent)
	}
}

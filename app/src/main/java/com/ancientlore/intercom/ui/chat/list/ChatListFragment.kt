package com.ancientlore.intercom.ui.chat.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.databinding.ChatListUiBinding
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.ui.dialog.option.chat.ChatOptionMenuDialog
import com.ancientlore.intercom.ui.dialog.option.chat.ChatOptionMenuParams

class ChatListFragment : FilterableFragment<ChatListViewModel, ChatListUiBinding>() {

	companion object {
		fun newInstance() = ChatListFragment()
	}

	override fun onBackPressed(): Boolean {
		return false
	}

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId() = R.menu.chat_list_menu

	override fun getLayoutResId() = R.layout.chat_list_ui

	override fun createDataBinding(view: View) = ChatListUiBinding.bind(view)

	override fun createViewModel() = ChatListViewModel(requireContext())

	override fun init(viewModel: ChatListViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		setHasOptionsMenu(true)

		dataBinding.listView.adapter = viewModel.listAdapter

		viewModel.init()

		subscriptions.add(viewModel.observeChatCreationRequest()
			.subscribe { openChatCreation() })
		subscriptions.add(viewModel.observeChatOpenRequest()
			.subscribe { openChatFlow(it) })
		subscriptions.add(viewModel.observeOpenChatMenuRequest()
			.subscribe { openChatMenu(it) })
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.createGroup -> {
				navigator?.openChatCreationGroup()
				true
			}
			R.id.contacts -> {
				navigator?.openContactList()
				true
			}
			R.id.settings -> {
				navigator?.openSettings()
				true
			}
			R.id.logout -> {
				App.backend.getAuthManager().logout()
				navigator?.openPhoneAuthForm()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun openChatCreation() = navigator?.openChatCreation()

	private fun openChatFlow(params: ChatFlowParams) = navigator?.openChatFlow(params)

	private fun openChatMenu(chat: Chat) {
		activity?.run {

			val dialog = ChatOptionMenuDialog
				.newInstance(ChatOptionMenuParams(pin = chat.pin == true, mute = chat.mute == true))

			dialog.listener = object : ChatOptionMenuDialog.Listener {
				override fun onPinClicked(pin: Boolean) {
					viewModel.switchChatPin(chat)
				}
				override fun onMuteClicked(pin: Boolean) {
					viewModel.switchChatMute(chat)
				}
			}

			dialog.show(supportFragmentManager, "ChatOptionMenuDialog")
		}
	}
}

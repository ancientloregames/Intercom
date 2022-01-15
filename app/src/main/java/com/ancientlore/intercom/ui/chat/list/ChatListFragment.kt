package com.ancientlore.intercom.ui.chat.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.databinding.ChatListUiBinding
import com.ancientlore.intercom.di.chat.list.ChatListScreenScope
import com.ancientlore.intercom.manager.DeviceContactsManager
import com.ancientlore.intercom.ui.FilterableFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.ui.dialog.option.chat.ChatOptionMenuDialog
import com.ancientlore.intercom.ui.dialog.option.chat.ChatOptionMenuParams
import javax.inject.Inject

@ChatListScreenScope
class ChatListFragment
	: FilterableFragment<ChatListViewModel, ChatListUiBinding>() {

	companion object {
		fun newInstance() = ChatListFragment()
	}

	@Inject
	protected lateinit var viewModel: ChatListViewModel

	override fun onBackPressed(): Boolean {
		return false
	}

	override fun getToolbar(): Toolbar = dataBinding.toolbar

	override fun getToolbarMenuResId() = R.menu.chat_list_menu

	override fun getLayoutResId() = R.layout.chat_list_ui

	override fun createDataBinding(view: View) = ChatListUiBinding.bind(view)

	override fun requestViewModel(): ChatListViewModel = viewModel

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		setHasOptionsMenu(true)

		dataBinding.listView.adapter = viewModel.getListAdapter()

		viewModel.init(DeviceContactsManager.getContacts(requireContext()))

		subscriptions.addAll(
			viewModel.chatCreationRequest().subscribe { openChatCreation() },

			viewModel.chatOpenRequest().subscribe { openChatFlow(it) },

			viewModel.openChatMenuRequest().subscribe { openChatMenu(it) },

			viewModel.createGroupRequest().subscribe { navigator?.openChatCreationGroup() },

			viewModel.openContactRequest().subscribe { navigator?.openContactList() },

			viewModel.openBroadcastRequest().subscribe { navigator?.openBroadcastList() },

			viewModel.openSettingsRequest().subscribe { navigator?.openSettings() },

			viewModel.openAuthFormRequest().subscribe { navigator?.openPhoneAuthForm() }
		)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.createGroup -> {
				viewModel.onOptionSelected(ChatListViewModel.OPTION_CREATE_GROUP)
				true
			}
			R.id.contacts -> {
				viewModel.onOptionSelected(ChatListViewModel.OPTION_OPEN_CONTACTS)
				true
			}
			R.id.broadcasts -> {
				viewModel.onOptionSelected(ChatListViewModel.OPTION_OPEN_BROADCAST)
				true
			}
			R.id.settings -> {
				viewModel.onOptionSelected(ChatListViewModel.OPTION_OPEN_SETTINGS)
				true
			}
			R.id.logout -> {
				viewModel.onOptionSelected(ChatListViewModel.OPTION_LOGOUT)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun openChatCreation() = navigator?.openChatCreation()

	private fun openChatFlow(params: ChatFlowParams) = navigator?.openChatFlow(params)

	private fun openChatMenu(chat: Chat) {
		activity?.run {

			val userId = App.backend.getAuthManager().getCurrentUserId()

			val dialog = ChatOptionMenuDialog
				.newInstance(ChatOptionMenuParams( // TODO all params must be prepared and given by the viewModel
					pin = chat.pin == true,
					mute = chat.mute == true,
					allowDelete = !chat.undeletable && chat.initiatorId == userId))

			dialog.listener = object : ChatOptionMenuDialog.Listener {
				override fun onPinClicked(pin: Boolean) {
					viewModel.onChatMenuOptionSelected(chat, ChatListViewModel.ITEM_OPTION_PIN)
				}
				override fun onMuteClicked(pin: Boolean) {
					viewModel.onChatMenuOptionSelected(chat, ChatListViewModel.ITEM_OPTION_MUTE)
				}
				override fun onDeleteClicked() {
					viewModel.onChatMenuOptionSelected(chat, ChatListViewModel.ITEM_OPTION_DELETE)
				}
			}

			dialog.show(supportFragmentManager, "ChatOptionMenuDialog")
		}
	}

	override fun getToastStringRes(toastId: Int): Int {
		return when (toastId) {
			ChatListViewModel.TOAST_CHAT_DELETED -> R.string.chat_deleted
			ChatListViewModel.TOAST_CHAT_DELETED_NOT -> R.string.chat_deleted_not
			ChatListViewModel.TOAST_CHAT_UNDELETABLE -> R.string.alert_chat_undeletable
			else -> return super.getToastStringRes(toastId)
		}
	}
}

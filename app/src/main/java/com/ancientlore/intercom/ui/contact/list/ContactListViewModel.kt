package com.ancientlore.intercom.ui.contact.list

import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.manager.DeviceContactsManager
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel : BasicViewModel(), DeviceContactsManager.UpdateListener {

	private lateinit var listAdapter: ContactListAdapter

	private val openChatSubj = PublishSubject.create<ChatFlowFragment.Params>() // ChatId

	fun init(listAdapter: ContactListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: DeviceContactsManager.Item) = openChat(contact)
		})

		listAdapter.setItems(DeviceContactsManager.getContactsCache())

		DeviceContactsManager.registerUpdateListener(this)
	}

	override fun clean() {
		DeviceContactsManager.unregisterUpdateListener(this)

		super.clean()
	}

	override fun onContactListUpdate(contacts: List<DeviceContactsManager.Item>) {
		listAdapter?.setItems(contacts)
	}

	fun observeChatOpen() = openChatSubj as Observable<ChatFlowFragment.Params>

	private fun openChat(contact: DeviceContactsManager.Item) {
		ChatRepository.getItem(contact.mainNumber, object : RequestCallback<Chat> {
			override fun onSuccess(chat: Chat) {
				openChatSubj.onNext(ChatFlowFragment.Params(chat.id, chat.name))
			}
			override fun onFailure(error: Throwable) {
				createAndOpenChat(contact)
			}
		})
	}

	private fun createAndOpenChat(contact: DeviceContactsManager.Item) {
		val userId = App.backend.getAuthManager().getCurrentUser()!!.id
		val chat = Chat(initiatorId = userId, participants = arrayOf(userId, contact.id))
		ChatRepository.addItem(chat, object : RequestCallback<String> {
			override fun onSuccess(chatId: String) {
				openChatSubj.onNext(ChatFlowFragment.Params(chatId, contact.name))
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				toastRequest.onNext(R.string.alert_error_creating_chat)
			}
		})
	}
}
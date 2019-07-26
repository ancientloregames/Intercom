package com.ancientlore.intercom.ui.contact.list

import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel : BasicViewModel() {

	private lateinit var listAdapter: ContactListAdapter

	private val openChatSubj = PublishSubject.create<ChatFlowFragment.Params>() // ChatId

	fun setListAdapter(listAdapter: ContactListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: Contact) = openChat(contact)
		})
		loadContactList()
	}

	fun observeChatOpen() = openChatSubj as Observable<ChatFlowFragment.Params>

	private fun loadContactList() {
		ContactRepository.getAll(object : SimpleRequestCallback<List<Contact>>() {
			override fun onSuccess(result: List<Contact>) {
				listAdapter.setItems(result)
			}
		})
	}

	private fun openChat(contact: Contact) {
		if (contact.chatId.isNotEmpty())
			openChatSubj.onNext(ChatFlowFragment.Params(contact.chatId, contact.name))
		else createAndOpenChat(contact)
	}

	private fun createAndOpenChat(contact: Contact) {
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
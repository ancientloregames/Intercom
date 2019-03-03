package com.ancientlore.intercom.ui.contact.list

import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ContactListViewModel : BasicViewModel() {

	private lateinit var listAdapter: ContactListAdapter

	private val openChatByIdEvent = PublishSubject.create<String>() // ChatId

	fun setListAdapter(listAdapter: ContactListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ContactListAdapter.Listener {
			override fun onContactSelected(contact: Contact) = openChat(contact)
		})
		loadContactList()
	}

	fun observeChatOpenById() = openChatByIdEvent as Observable<String>

	private fun loadContactList() {
		ContactRepository.getAll(object : SimpleRequestCallback<List<Contact>>() {
			override fun onSuccess(result: List<Contact>) {
				listAdapter.setItems(result)
			}
		})
	}

	private fun openChat(contact: Contact) {
		if (contact.chatId.isNotEmpty()) {
			ChatRepository.getItem(contact.chatId, object : RequestCallback<Chat> {
				override fun onSuccess(result: Chat) {
					openChatByIdEvent.onNext(result.chatId)
				}
				override fun onFailure(error: Throwable) {
					Utils.logError(error)
					toastRequest.onNext(R.string.alert_error_opening_chat)
				}
			})
		}
		else createAndOpenChat(contact)
	}

	private fun createAndOpenChat(contact: Contact) {
		ChatRepository.createDialog(contact.phone, object : RequestCallback<String> {
			override fun onSuccess(result: String) {
				openChatByIdEvent.onNext(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				toastRequest.onNext(R.string.alert_error_creating_chat)
			}
		})
	}
}
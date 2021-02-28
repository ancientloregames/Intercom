package com.ancientlore.intercom.ui.chat.list

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
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
import java.util.*

class ChatListViewModel : BasicViewModel() {

	private lateinit var listAdapter: ChatListAdapter

	private val contactListRequest = PublishSubject.create<Any>()
	private val openChatOpenSubj = PublishSubject.create<ChatFlowFragment.Params>()

	private var repositorySub: RepositorySubscription? = null

	override fun clean() {
		repositorySub?.remove()

		super.clean()
	}

	fun init(listAdapter: ChatListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) {
				openChatOpenSubj.onNext(ChatFlowFragment.Params(chat.id, chat.localName ?: chat.name))
			}
		})
		attachDataListener()
	}

	fun onShowContactListClicked() = contactListRequest.onNext(EmptyObject)

	fun observeContactListRequest() = contactListRequest as Observable<*>
	fun observeChatOpen() = openChatOpenSubj as Observable<ChatFlowFragment.Params>

	private fun attachDataListener() {
		//TODO load chats independantly of contact list and assign names postpone
		ContactRepository.getAll(object  : RequestCallback<List<Contact>> {
			override fun onSuccess(contacts: List<Contact>) {
				repositorySub = ChatRepository.attachListener(object : SimpleRequestCallback<List<Chat>>() {
					override fun onSuccess(chats: List<Chat>) {
						assignPrivateChatNames(chats, contacts)
						listAdapter.setItems(chats)
					}
					override fun onFailure(error: Throwable) { Utils.logError(error) }
				})
			}
			override fun onFailure(error: Throwable) { Utils.logError(error) }
		})
	}

	private fun assignPrivateChatNames(chats: List<Chat>, contacts: List<Contact>) {

		val contactListTmp = LinkedList(contacts)
		for (chat in chats) {

			val contactListIter = contactListTmp.iterator()
			while (contactListIter.hasNext()) {
				val contact = contactListIter.next()

				if (chat.name == contact.phone) {
					chat.localName = contact.name
					contactListIter.remove()
					break
				}
			}
		}
	}

	fun filter(text: String) {
		listAdapter.filter(text)
	}
}

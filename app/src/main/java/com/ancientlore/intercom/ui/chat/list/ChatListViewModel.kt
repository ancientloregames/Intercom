package com.ancientlore.intercom.ui.chat.list

import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class ChatListViewModel(listAdapter: ChatListAdapter)
	: FilterableViewModel<ChatListAdapter>(listAdapter) {

	private val chatCreationSub = PublishSubject.create<Any>()
	private val chatOpenSub = PublishSubject.create<ChatFlowParams>()

	private var repositorySub: RepositorySubscription? = null

	override fun clean() {
		chatCreationSub.onComplete()
		chatOpenSub.onComplete()
		repositorySub?.remove()

		super.clean()
	}

	fun init() {
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) {
				chatOpenSub.onNext(ChatFlowParams(
					userId = App.backend.getAuthManager().getCurrentUser().id,
					chatId = chat.id,
					title = chat.localName ?: chat.name))
			}
		})
		attachDataListener()
	}

	fun onCreateChatClicked() = chatCreationSub.onNext(EmptyObject)

	fun observeChatCreationRequest() = chatCreationSub as Observable<*>
	fun observeChatOpenRequest() = chatOpenSub as Observable<ChatFlowParams>

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
}

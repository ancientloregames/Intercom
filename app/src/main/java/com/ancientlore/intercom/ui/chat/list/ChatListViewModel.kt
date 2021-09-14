package com.ancientlore.intercom.ui.chat.list

import androidx.databinding.ObservableBoolean
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Chat.Companion.TYPE_PRIVATE
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class ChatListViewModel(listAdapter: ChatListAdapter)
	: FilterableViewModel<ChatListAdapter>(listAdapter) {

	val chatListIsEmpty = ObservableBoolean(false)

	val chatListFirstLoad = ObservableBoolean(false)

	private val chatCreationSub = PublishSubject.create<Any>()
	private val chatOpenSub = PublishSubject.create<ChatFlowParams>()
	private val openChatMenuSub = PublishSubject.create<Chat>()

	private var repositorySub: RepositorySubscription? = null

	override fun clean() {
		chatCreationSub.onComplete()
		chatOpenSub.onComplete()
		openChatMenuSub.onComplete()
		repositorySub?.remove()
		listAdapter.setListener(null)

		super.clean()
	}

	fun init() {
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) {

				val userId = App.backend.getAuthManager().getCurrentUser().id

				val participants = if (chat.type == TYPE_PRIVATE) {
					listOf(userId, chat.name)
				} else chat.participants

				chatOpenSub.onNext(ChatFlowParams(
					userId = userId,
					chatId = chat.id,
					chatType = chat.type,
					title = chat.localName ?: chat.name,
					iconUri = chat.iconUri,
					participants = participants))
			}
			override fun onItemLongClick(chat: Chat) {
				openChatMenuSub.onNext(chat)
			}
		})
		attachDataListener()
	}

	fun onCreateChatClicked() = chatCreationSub.onNext(EmptyObject)

	fun observeChatCreationRequest() = chatCreationSub as Observable<*>
	fun observeChatOpenRequest() = chatOpenSub as Observable<ChatFlowParams>
	fun observeOpenChatMenuRequest() = openChatMenuSub as Observable<Chat>

	private fun attachDataListener() {
		//TODO load chats independantly of contact list and assign names postpone
		ContactRepository.getAll(object  : CrashlyticsRequestCallback<List<Contact>>() {
			override fun onSuccess(contacts: List<Contact>) {
				repositorySub = ChatRepository.attachListener(object : CrashlyticsRequestCallback<List<Chat>>() {
					override fun onSuccess(chats: List<Chat>) {

						assignPrivateChatNames(chats, contacts)
						runOnUiThread {
							chatListIsEmpty.set(chats.isEmpty())
							listAdapter.setItems(chats)
							chatListFirstLoad.set(true)
						}
					}
					override fun onFailure(error: Throwable) {
						super.onFailure(error)
						chatListFirstLoad.set(true)
						chatListIsEmpty.set(true)
					}
				})
			}
		})
	}

	private fun assignPrivateChatNames(chats: List<Chat>, contacts: List<Contact>) {

		val contactListTmp = LinkedList(contacts)
		for (chat in chats) {

			val contactListIter = contactListTmp.iterator()
			while (contactListIter.hasNext()) {
				val contact = contactListIter.next()

				if (chat.lastMsgSenderId == contact.phone) {
					chat.lastMsgSenderLocalName = contact.name
				}

				if (chat.name == contact.phone) {
					chat.localName = contact.name
					contactListIter.remove()
					break
				}
			}
		}
	}

	fun switchChatPin(chat: Chat) {
		ChatRepository.updateItem(Chat(
			id = chat.id,
			name = chat.name,
			type = chat.type,
			pin = chat.pin?.not() ?: false,
			participants = chat.participants
		))
	}

	fun switchChatMute(chat: Chat) {
		ChatRepository.updateItem(Chat(
			id = chat.id,
			name = chat.name,
			type = chat.type,
			mute = chat.mute?.not() ?: false,
			participants = chat.participants
		))
	}
}

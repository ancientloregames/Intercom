package com.ancientlore.intercom.ui.chat.list

import android.content.Context
import androidx.annotation.IntDef
import androidx.databinding.ObservableBoolean
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Chat.Companion.TYPE_PRIVATE
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.manager.DeviceContactsManager
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class ChatListViewModel(context: Context)
	: FilterableViewModel<ChatListAdapter>(ChatListAdapter(context)) {

	companion object {
		const val ITEM_OPTION_PIN = 0
		const val ITEM_OPTION_MUTE = 1
		const val ITEM_OPTION_DELETE = 2

		const val TOAST_CHAT_DELETED = 0
		const val TOAST_CHAT_DELETED_NOT = 1
		const val TOAST_CHAT_UNDELETABLE = 2
	}

	@IntDef(ITEM_OPTION_PIN, ITEM_OPTION_MUTE, ITEM_OPTION_DELETE)
	@Retention(AnnotationRetention.SOURCE)
	annotation class ItemOption

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

	fun init(contacts: List<DeviceContactsManager.Item>) {

		repositorySub = ChatRepository.attachListener(object : CrashlyticsRequestCallback<List<Chat>>() {
			override fun onSuccess(chats: List<Chat>) {

				assignPrivateChatNames(chats, contacts)
				runOnUiThread {
					chatListIsEmpty.set(chats.isEmpty())
					chatListFirstLoad.set(true)
					listAdapter.setItems(chats)
				}
			}
			override fun onFailure(error: Throwable) {
				super.onFailure(error)
				chatListIsEmpty.set(true)
				chatListFirstLoad.set(true)
			}
		})

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
	}

	fun onCreateChatClicked() = chatCreationSub.onNext(EmptyObject)

	fun observeChatCreationRequest() = chatCreationSub as Observable<*>
	fun observeChatOpenRequest() = chatOpenSub as Observable<ChatFlowParams>
	fun observeOpenChatMenuRequest() = openChatMenuSub as Observable<Chat>

	fun assignPrivateChatNames(chats: List<Chat>, contacts: List<DeviceContactsManager.Item>) {

		val contactListTmp = LinkedList(contacts)
		for (chat in chats) {

			val contactListIter = contactListTmp.iterator()
			while (contactListIter.hasNext()) {
				val contact = contactListIter.next()

				if (chat.lastMsgSenderId == contact.formatedMainNumber) {
					chat.lastMsgSenderLocalName = contact.name
				}

				if (chat.name == contact.formatedMainNumber) {
					chat.localName = contact.name
					contactListIter.remove()
					break
				}
			}
		}
	}

	fun onChatMenuOptionSelected(chat: Chat, @ItemOption id: Int) {
		when (id) {
			ITEM_OPTION_PIN -> switchChatPin(chat)
			ITEM_OPTION_MUTE -> switchChatMute(chat)
			ITEM_OPTION_DELETE -> tryDeleteChat(chat)
		}
	}

	private fun switchChatPin(chat: Chat) {
		ChatRepository.updateItem(Chat(
			id = chat.id,
			name = chat.name,
			type = chat.type,
			pin = chat.pin?.not() ?: false,
			participants = chat.participants
		))
	}

	private fun switchChatMute(chat: Chat) {
		ChatRepository.updateItem(Chat(
			id = chat.id,
			name = chat.name,
			type = chat.type,
			mute = chat.mute?.not() ?: false,
			participants = chat.participants
		))
	}

	private fun tryDeleteChat(chat: Chat) {

		if (chat.undeletable.not()) {
			ChatRepository.deleteItem(chat.id, object : CrashlyticsRequestCallback<Any>() {
				override fun onSuccess(result: Any) {
					toastRequest.onNext(TOAST_CHAT_DELETED)
				}
				override fun onFailure(error: Throwable) {
					super.onFailure(error)
					toastRequest.onNext(TOAST_CHAT_DELETED_NOT)
				}
			})
		}
		else {
			toastRequest.onNext(TOAST_CHAT_UNDELETABLE)
		}
	}
}

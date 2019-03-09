package com.ancientlore.intercom.ui.chat.list

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatListViewModel : BasicViewModel() {

	private lateinit var listAdapter: ChatListAdapter

	private val contactListRequest = PublishSubject.create<Any>()
	private val openChatByIdEvent = PublishSubject.create<String>() // Chat Id

	fun setListAdapter(listAdapter: ChatListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) = openChatByIdEvent.onNext(chat.chatId)
		})
		loadChatList()
	}

	fun onShowContactListClicked() = contactListRequest.onNext(EmptyObject)

	fun observeContactListRequest() = contactListRequest as Observable<*>
	fun observeChatOpenById() = openChatByIdEvent as Observable<String>

	private fun loadChatList() {
		ChatRepository.getAll(object : SimpleRequestCallback<List<Chat>>() {
			override fun onSuccess(result: List<Chat>) {
				listAdapter.setItems(result)
			}
		})
	}
}

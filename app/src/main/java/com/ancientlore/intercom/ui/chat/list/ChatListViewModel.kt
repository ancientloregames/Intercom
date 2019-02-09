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

	fun setListadapter(listAdapter: ChatListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) = chatSelectedEvent.onNext(chat)
		})
		loadChatList()
	}

	private val contactListRequest = PublishSubject.create<Any>()
	private val chatSelectedEvent = PublishSubject.create<Chat>()

	fun onShowContactListClicked() = contactListRequest.onNext(EmptyObject)

	fun observeContactListRequest() = contactListRequest as Observable<*>
	fun observeChatSelectedEvent() = chatSelectedEvent as Observable<Chat>

	private fun loadChatList() {
		ChatRepository.getAll(object : SimpleRequestCallback<List<Chat>>() {
			override fun onSuccess(result: List<Chat>) {
				listAdapter.setItems(result)
			}
		})
	}
}

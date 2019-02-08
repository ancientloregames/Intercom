package com.ancientlore.intercom.ui.chat.list

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.ui.BasicViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatListViewModel : BasicViewModel() {

	private var listAdapter: ChatListAdapter? = null

	private val contactListRequest = PublishSubject.create<Any>()
	private val chatSelectedEvent = PublishSubject.create<Chat>()

	fun init(listAdapter: ChatListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) = chatSelectedEvent.onNext(chat)
		})
	}

	fun onShowContactListClicked() = contactListRequest.onNext(EmptyObject)

	fun observeContactListRequest() = contactListRequest as Observable<*>
}

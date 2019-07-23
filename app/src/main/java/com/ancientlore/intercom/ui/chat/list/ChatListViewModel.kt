package com.ancientlore.intercom.ui.chat.list

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ChatListViewModel : BasicViewModel() {

	private lateinit var listAdapter: ChatListAdapter

	private val contactListRequest = PublishSubject.create<Any>()
	private val openChatOpenSubj = PublishSubject.create<ChatFlowFragment.Params>()

	override fun onCleared() {
		super.onCleared()
		detachDataListener()
	}

	fun setListAdapter(listAdapter: ChatListAdapter) {
		this.listAdapter = listAdapter
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) {
				openChatOpenSubj.onNext(ChatFlowFragment.Params(chat.id, chat.name))
			}
		})
		attachDataListener()
	}

	fun onShowContactListClicked() = contactListRequest.onNext(EmptyObject)

	fun observeContactListRequest() = contactListRequest as Observable<*>
	fun observeChatOpen() = openChatOpenSubj as Observable<ChatFlowFragment.Params>

	private fun attachDataListener() {
		ChatRepository.attachListener(object : SimpleRequestCallback<List<Chat>>() {
			override fun onSuccess(result: List<Chat>) {
				listAdapter.setItems(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
			}
		})
	}

	private fun detachDataListener() {
		ChatRepository.detachListener()
	}
}

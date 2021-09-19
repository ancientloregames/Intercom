package com.ancientlore.intercom.ui.boadcast.list

import android.content.Context
import androidx.databinding.ObservableBoolean
import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.CrashlyticsRequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.ui.FilterableViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import com.ancientlore.intercom.ui.chat.list.ChatListAdapter
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class BroadcastListViewModel(context: Context)
	: FilterableViewModel<ChatListAdapter>(ChatListAdapter(context)) {

	val broadcastListIsEmpty = ObservableBoolean(false)

	private val openBroadcastSubj = PublishSubject.create<ChatFlowParams>()

	init {
		listAdapter.setListener(object : ChatListAdapter.Listener {
			override fun onChatSelected(chat: Chat) {
				openBroadcastSubj.onNext(ChatFlowParams(
					userId = App.backend.getAuthManager().getCurrentUserId(),
					chatId = chat.id,
					chatType = chat.type,
					title = chat.name,
					iconUri = chat.iconUri,
					participants = chat.participants
				))
			}
			override fun onItemLongClick(chat: Chat) {
			}
		})

		ChatRepository.getBroadcasts(object : CrashlyticsRequestCallback<List<Chat>>() {
			override fun onSuccess(result: List<Chat>) {
				runOnUiThread {
					listAdapter.setItems(result)
					broadcastListIsEmpty.set(result.isEmpty())
				}
			}

			override fun onFailure(error: Throwable) {
				super.onFailure(error)
				broadcastListIsEmpty.set(true)
			}
		})
	}

	override fun clean() {
		openBroadcastSubj.onComplete()

		super.clean()
	}

	fun openBroadcastRequest() = openBroadcastSubj as Observable<ChatFlowParams>
}
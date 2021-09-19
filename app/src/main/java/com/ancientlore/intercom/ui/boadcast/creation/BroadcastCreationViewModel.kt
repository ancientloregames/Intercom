package com.ancientlore.intercom.ui.boadcast.creation

import android.net.Uri
import androidx.databinding.ObservableField
import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class BroadcastCreationViewModel: BasicViewModel() {

	val broadcastIconField = ObservableField(Uri.EMPTY)
	val broadcastTitleField = ObservableField("")

	private val pickIconSubj = PublishSubject.create<EmptyObject>()
	private val createBroadcastSubj = PublishSubject.create<ChatFlowParams>()

	override fun clean() {
		pickIconSubj.onComplete()
		createBroadcastSubj.onComplete()

		super.clean()
	}

	fun onIconClick() {
		pickIconSubj.onNext(EmptyObject)
	}

	fun onDoneButtonClick() {
		val userId = App.backend.getAuthManager().getCurrentUserId()
		createBroadcastSubj.onNext(ChatFlowParams(
			userId = userId,
			chatType = Chat.TYPE_BROADCAST,
			title = broadcastTitleField.get()!!,
			iconUri = broadcastIconField.get()!!,
			participants = listOf(userId)
		))
	}

	fun onIconPicked(uri: Uri) {
		broadcastIconField.set(uri)
	}

	fun pickIconRequest() = pickIconSubj as Observable<Any>

	fun createBroadcastRequest() = createBroadcastSubj as Observable<ChatFlowParams>
}
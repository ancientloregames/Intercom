package com.ancientlore.intercom.ui.notification

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.data.model.PushMessage
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class NotificationAnswerViewModel(val message: PushMessage) : ViewModel() {

	val messageField = ObservableField(message.body)

	val replyField = ObservableField("")

	private val onSendClick = PublishSubject.create<String>()

	private val onCancelClick = PublishSubject.create<Any>()


	fun onSend() {
		val replyText = replyField.get()!!
		if (replyText.isNotEmpty())
			onSendClick.onNext(replyText)
	}

	fun onCancel() {
		onCancelClick.onNext(EmptyObject)
	}

	fun observeSendClicked() = onSendClick as Observable<String>

	fun observeCancelClicked() = onCancelClick as Observable<*>
}
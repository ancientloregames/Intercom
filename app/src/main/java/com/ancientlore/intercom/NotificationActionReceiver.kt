package com.ancientlore.intercom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.model.PushMessage
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.utils.NotificationManager
import java.lang.RuntimeException

class NotificationActionReceiver: BroadcastReceiver() {

	companion object {
		const val ACTION_REPLY = "com.ancientlore.intercom.action.REPLY"
		const val RESULT_REPLY = "replyText"
	}

	override fun onReceive(context: Context, intent: Intent) {
		when (intent.action) {
			ACTION_REPLY -> {
				val message = getMessage(intent)

				sendReply(message.chatId, getReplyText(intent))

				NotificationManager.getInstance(context)
					.cancelNotification(getNotificationId(intent))
			}
		}
	}

	private fun getReplyText(intent: Intent): String {
		return RemoteInput.getResultsFromIntent(intent)
			?.let { remoteInput ->
				remoteInput.getCharSequence(RESULT_REPLY)?.toString()
					?: throw RuntimeException("Error! No reply text.")
			}
			?: intent.getStringExtra(RESULT_REPLY)
			?: throw RuntimeException("Error! No reply text.")
	}

	private fun getMessage(intent: Intent) = intent.getParcelableExtra<PushMessage>(NotificationManager.EXTRA_MESSAGE)
		?: throw RuntimeException("Message mandatory!")

	private fun getNotificationId(intent: Intent) = intent.getIntExtra(NotificationManager.EXTRA_NOTIFICATION_ID, -1)
		.takeIf { it != -1 } ?: throw RuntimeException("Error! Notification Id in extras is mandatory.")

	private fun sendReply(chatId: String, replyText: String) {
		MessageRepository().apply {
			setRemoteSource(App.backend.getDataSourceProvider().getMessageSource(chatId))

			val user = App.backend.getAuthManager().getCurrentUser()!!
			val replyMessage = Message(senderId = user.id, text = replyText)

			addMessage(replyMessage, object : SimpleRequestCallback<String>("MessageReply") {})
		}
	}
}
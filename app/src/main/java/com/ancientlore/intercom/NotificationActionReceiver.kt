package com.ancientlore.intercom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.model.PushMessage
import com.ancientlore.intercom.data.source.MessageRepository
import com.ancientlore.intercom.utils.NotificationManager
import java.lang.RuntimeException

class NotificationActionReceiver: BroadcastReceiver() {

	companion object {
		const val ACTION_READ = "com.ancientlore.intercom.action.READ"
		const val ACTION_REPLY = "com.ancientlore.intercom.action.REPLY"
		const val RESULT_REPLY = "replyText"
	}

	override fun onReceive(context: Context, intent: Intent) {
		when (intent.action) {
			ACTION_REPLY -> {
				val message = getMessage(intent)

				MessageRepository().apply {
					setRemoteSource(App.backend.getDataSourceProvider().getMessageSource(message.chatId))
					setRemoteSource(App.frontend.getDataSourceProvider().getMessageSource(message.chatId))
					markAsRead(message.id)
					sendReply(getReplyText(intent))
				}

				NotificationManager.getInstance(context)
					.cancelNotification(getNotificationId(intent))
			}
			ACTION_READ -> {
				MessageRepository().apply {
					setRemoteSource(App.backend.getDataSourceProvider().getMessageSource(getChatId(intent)))
					setRemoteSource(App.frontend.getDataSourceProvider().getMessageSource(getChatId(intent)))
					markAsRead(getMessageId(intent))
				}

				NotificationManager.getInstance(context)
					.cancelNotification(getNotificationId(intent))
			}
		}
	}

	private fun MessageRepository.sendReply(replyText: String) {
		val user = App.backend.getAuthManager().getCurrentUser()!!
		val replyMessage = Message(senderId = user.id, text = replyText)

		addItem(replyMessage)
	}

	private fun MessageRepository.markAsRead(messageId: String) {
		setMessageStatusReceived(messageId)
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

	private fun getMessageId(intent: Intent) = intent.getStringExtra(NotificationManager.EXTRA_MESSAGE_ID)
		?: throw RuntimeException("Error! Message Id in extras is mandatory.")

	private fun getChatId(intent: Intent) = intent.getStringExtra(NotificationManager.EXTRA_CHAT_ID)
		?: throw RuntimeException("Error! Chat Id in extras is mandatory.")
}
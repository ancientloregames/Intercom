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
		private val PACKAGE = NotificationActionReceiver::class.java.getPackage()!!.toString()
		val ACTION_REPLY = "$PACKAGE.action.REPLY"
		val RESULT_REPLY = "$PACKAGE.result.REPLY"
	}

	override fun onReceive(context: Context, intent: Intent) {
		when (intent.action) {
			ACTION_REPLY -> {
				intent.getParcelableExtra<PushMessage>(NotificationManager.EXTRA_MESSAGE)
					?.let { message ->
						val replyText = getReplyText(intent)

						sendReply(message, replyText)

						val notificationId = intent.getIntExtra(NotificationManager.EXTRA_NOTIFICATION_ID, 0)

						val replyMessage = PushMessage().apply {
							title = context.getString(R.string.you)
							body = replyText
							chatId = message.chatId
						}
						NotificationManager.getInstance(context)
							.updateNotification(notificationId, replyMessage)
					} ?: throw RuntimeException("Error! No message to reply on.")
			}
		}
	}

	private fun getReplyText(intent: Intent): String {
		return RemoteInput.getResultsFromIntent(intent)
			?.let { remoteInput ->
				remoteInput.getCharSequence(RESULT_REPLY)?.toString()
					?: throw RuntimeException("Error! No reply text.")
			} ?: throw RuntimeException("Error! No reply text.")
	}

	private fun sendReply(message: PushMessage, replyText: String) {
		MessageRepository().apply {
			setRemoteSource(App.backend.getDataSourceProvider().getMessageSource(message.chatId))

			val user = App.backend.getAuthManager().getCurrentUser()!!
			val replyMessage = Message(senderId = user.id, text = replyText)

			addMessage(replyMessage, object : SimpleRequestCallback<String>("MessageReply") {})
		}
	}
}
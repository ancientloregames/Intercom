package com.ancientlore.intercom.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.res.ResourcesCompat
import com.ancientlore.intercom.ui.notification.NotificationAnswerActivity
import com.ancientlore.intercom.NotificationActionReceiver
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.PushMessage

class NotificationManager private constructor(private val context: Context) {

	internal companion object : SingletonHolder<NotificationManager, Context>(
		{ context -> NotificationManager(context) }) {

		const val EXTRA_NOTIFICATION_ID = "not_id"
		const val EXTRA_MESSAGE = "message"

		private var idCounter = 0
	}

	private val manager: NotificationManagerCompat by lazy { NotificationManagerCompat.from(context) }

	private val resources: Resources get() = context.resources

	fun showNotification(message: PushMessage) {
		manager.notify(idCounter, createNotification(message))
		idCounter++
	}

	fun cancelNotification(id: Int) {
		manager.cancel(id)
	}

	private fun createNotification(message: PushMessage): Notification {
		val builder = NotificationCompat.Builder(context, getNotificationChannelId(message.type))
			.setTicker(getTickerText((message)))
			.setContentTitle(message.title)
			.setContentText(message.body)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setDefaults(NotificationCompat.DEFAULT_ALL)
			.setColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
			.setSmallIcon(R.drawable.ic_notification)
			.setWhen(System.currentTimeMillis())
			.setShowWhen(true)
			.setAutoCancel(true)

		if (message.isReplyable)
			builder.addReplyActions(message)

		return builder.build()
	}

	private fun getNotificationChannelId(messageType: String) : String {
		return when (messageType) {
			PushMessage.TYPE_CHAT_MESSAGE -> resources.getString(R.string.chat_notification_channel_id)
			else -> resources.getString(R.string.basic_notification_channel_id)
		}
	}

	private fun getTickerText(message: PushMessage) : String {
		return when (message.type) {
			PushMessage.TYPE_CHAT_MESSAGE -> resources.getString(R.string.chat_notification_ticker, message.title)
			else -> resources.getString(R.string.app_name)
		}
	}

	private fun createPendingIntent(extras: Bundle): PendingIntent {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			val intent = Intent(context, NotificationActionReceiver::class.java).apply {
				action = NotificationActionReceiver.ACTION_REPLY
				putExtras(extras)
			}
			PendingIntent.getBroadcast(context, idCounter, intent, PendingIntent.FLAG_CANCEL_CURRENT)
		} else {
			val intent = Intent(context, NotificationAnswerActivity::class.java).apply {
				action = NotificationActionReceiver.ACTION_REPLY
				flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				putExtras(extras)
			}
			PendingIntent.getActivity(context, idCounter, intent, PendingIntent.FLAG_CANCEL_CURRENT)
		}
	}

	private fun createReplyAction(pendingIntent: PendingIntent): NotificationCompat.Action {
		return NotificationCompat.Action.Builder(
			R.drawable.ic_reply, context.getString(R.string.reply), pendingIntent)
			.addRemoteInput(createReplyInput())
			.build()
	}

	private fun createReplyInput(): RemoteInput {
		return RemoteInput.Builder(NotificationActionReceiver.RESULT_REPLY)
			.setChoices(resources.getStringArray(R.array.quick_reply_choices))
			.setLabel(context.getString(R.string.reply))
			.build()
	}

	private fun NotificationCompat.Builder.addReplyActions(message: PushMessage) {
		val replyParams = Bundle().apply {
			putParcelable(EXTRA_MESSAGE, message)
			putInt(EXTRA_NOTIFICATION_ID, idCounter)
		}
		val pendingIntent = createPendingIntent(replyParams)
		val replyAction = createReplyAction(pendingIntent)

		addAction(replyAction)
		extend(NotificationCompat.WearableExtender().addAction(replyAction))
	}
}
package com.ancientlore.intercom.utils

import android.app.Notification
import android.content.Context
import android.content.res.Resources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.PushMessage

class NotificationManager private constructor(private val context: Context) {

	internal companion object : SingletonHolder<NotificationManager, Context>(
		{ context -> NotificationManager(context) }) {
		private var idCounter = 0
	}

	private val resources: Resources get() = context.resources

	fun showNotification(message: PushMessage) {
		val notification = createNotification(message)

		NotificationManagerCompat.from(context).notify(idCounter, notification)
		idCounter++
	}

	private fun createNotification(message: PushMessage): Notification {
		return NotificationCompat.Builder(context, getNotificationChannelId(message.type))
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
			.build()
	}

	private fun getNotificationChannelId(messageType: Int) : String {
		return when (messageType) {
			PushMessage.TYPE_CHAT_MESSAGE -> resources.getString(R.string.chat_notification_channel_id)
			else -> ""
		}
	}

	private fun getTickerText(message: PushMessage) : String {
		return when (message.type) {
			PushMessage.TYPE_CHAT_MESSAGE -> resources.getString(R.string.chat_notification_ticker, message.title)
			else -> ""
		}
	}
}
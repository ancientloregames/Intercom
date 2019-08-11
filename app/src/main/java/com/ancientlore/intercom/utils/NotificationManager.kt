package com.ancientlore.intercom.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.res.ResourcesCompat
import com.ancientlore.intercom.MainActivity
import com.ancientlore.intercom.ui.notification.NotificationAnswerActivity
import com.ancientlore.intercom.NotificationActionReceiver
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.PushMessage
import kotlin.math.abs

class NotificationManager private constructor(private val context: Context) {

	internal companion object : SingletonHolder<NotificationManager, Context>(
		{ context -> NotificationManager(context) }) {

		const val ACTION_OPEN_FROM_PUSH = "com.ancientlore.intercom.action.OPEN_FROM_PUSH"

		const val EXTRA_NOTIFICATION_ID = "not_id"
		const val EXTRA_MESSAGE = "message"
		const val EXTRA_CHAT_ID = "chat_id"
		const val EXTRA_MESSAGE_ID = "message_id"
		const val EXTRA_CHAT_TITLE = "chat_title"

		private var currentId = 0
		private var idCounter = -1
	}

	private val manager: NotificationManagerCompat by lazy { NotificationManagerCompat.from(context) }

	private val resources: Resources get() = context.resources

	fun showNotification(message: PushMessage) {
		currentId = getNotificationId(message)
		manager.notify(currentId, createNotification(message))
	}

	fun cancelNotification(id: Int) {
		manager.cancel(id)
	}

	private fun getNotificationId(message: PushMessage) : Int {
		return if (message.hasChatId()) {
			abs(message.chatId.hashCode())
		} else {
			idCounter++
			idCounter
		}
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
			.setLargeIcon(getIcon(message))
			.setWhen(System.currentTimeMillis())
			.setShowWhen(true)
			.setAutoCancel(true)

		if (message.isReplyable) {
			builder.addReplyActions(message)
				.addAction(createReadAction(message))
				.setContentIntent(createContentIntent(message))
		}

		return builder.build()
	}

	private fun getIcon(message: PushMessage): Bitmap {
		// TODO download image, if message has icon url
		return BitmapFactory.decodeResource(resources, R.drawable.ic_avatar_placeholder)
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

	private fun createContentIntent(message: PushMessage) : PendingIntent {
		val intent = when (message.type) {
			PushMessage.TYPE_CHAT_MESSAGE -> {
				Intent(context, MainActivity::class.java).apply {
					action = ACTION_OPEN_FROM_PUSH
					flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
					putExtras(Bundle().apply {
						putString(EXTRA_CHAT_ID, message.chatId)
						putString(EXTRA_CHAT_TITLE, message.title)
					})
				}
			}
			else -> {
				Intent(context, MainActivity::class.java).apply {
					action = Intent.ACTION_MAIN
					addCategory(Intent.CATEGORY_LAUNCHER)
					flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
				}
			}
		}
		return PendingIntent.getActivity(context, currentId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
	}

	private fun createPendingIntent(extras: Bundle): PendingIntent {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			val intent = Intent(context, NotificationActionReceiver::class.java).apply {
				action = NotificationActionReceiver.ACTION_REPLY
				putExtras(extras)
			}
			PendingIntent.getBroadcast(context, currentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
		} else {
			val intent = Intent(context, NotificationAnswerActivity::class.java).apply {
				action = NotificationActionReceiver.ACTION_REPLY
				flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				putExtras(extras)
			}
			PendingIntent.getActivity(context, currentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
		}
	}

	private fun createReadAction(message: PushMessage): NotificationCompat.Action {
		val intent = Intent(context, NotificationActionReceiver::class.java).apply {
			action = NotificationActionReceiver.ACTION_READ
			putExtras(Bundle().apply {
				putString(EXTRA_MESSAGE_ID, message.id)
				putString(EXTRA_CHAT_ID, message.chatId)
				putInt(EXTRA_NOTIFICATION_ID, currentId)
			})
		}

		val pendingIntent = PendingIntent.getBroadcast(context, currentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
		return NotificationCompat.Action(0, resources.getString(R.string.mark_as_read), pendingIntent)
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

	private fun NotificationCompat.Builder.addReplyActions(message: PushMessage): NotificationCompat.Builder {
		val replyParams = Bundle().apply {
			putString(EXTRA_MESSAGE_ID, message.id)
			putParcelable(EXTRA_MESSAGE, message)
			putInt(EXTRA_NOTIFICATION_ID, currentId)
		}
		val pendingIntent = createPendingIntent(replyParams)
		val replyAction = createReplyAction(pendingIntent)

		addAction(replyAction)
		extend(NotificationCompat.WearableExtender().addAction(replyAction))
		return this
	}
}
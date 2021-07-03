package com.ancientlore.intercom

import com.ancientlore.intercom.data.model.PushMessage
import com.ancientlore.intercom.data.source.UserRepository
import com.ancientlore.intercom.utils.JsonUtils
import com.ancientlore.intercom.utils.NotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService: FirebaseMessagingService() {

	private val manager: NotificationManager = NotificationManager.getInstance(this)

	override fun onMessageReceived(remoteMessage: RemoteMessage?) {
		super.onMessageReceived(remoteMessage)

		if (MainActivity.isInBackground && remoteMessage?.data != null) {
			val message = JsonUtils.deserialize(remoteMessage.data, PushMessage::class.java)
			manager.showNotification(message)
		}
	}

	override fun onNewToken(newToken: String?) {
		newToken?.let { token ->
			App.backend.getAuthManager().getCurrentUser()
				?.let { user ->
					if (!UserRepository.hasRemoteSource()) {
						val remoteSource = App.backend.getDataSourceProvider().getUserSource(user.id)
						UserRepository.setRemoteSource(remoteSource)
					}

					UserRepository.updateNotificationToken(token)
				}
		}
	}
}
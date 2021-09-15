package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.model.PushMessage
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_MSG_SENDER
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_MSG_TEXT
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_MSG_TIME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_STATUS
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class FirestoreMessageSourceNoCF(chatId: String): FirestoreMessageSource(chatId) {

	override fun addItem(item: Message, callback: RequestCallback<String>) {

		chatMessages.add(item)
			.addOnSuccessListener {

				val senderId = item.senderId
				val messageId = it.id

				chatMessages.document(messageId)
					.set(HashMap<String, Any>().apply {
						put(FIELD_STATUS, Message.STATUS_SENT)
					}, SetOptions.merge())
					.addOnFailureListener { exec { callback.onFailure(it) } }

				val userChatInfoUpdate = HashMap<String, Any>().apply {
					put(FIELD_LAST_MSG_SENDER, senderId)
					put(FIELD_LAST_MSG_TEXT, item.text)
					put(FIELD_LAST_MSG_TIME, FieldValue.serverTimestamp())
				}

				for (receiverId in item.receivers) {
					db.collection(USERS)
						.document(receiverId)
						.collection(CHATS)
						.document(getSourceId())
						.set(userChatInfoUpdate, SetOptions.merge())
						.addOnSuccessListener {
							if (item.senderId == receiverId)
								exec { callback.onSuccess(messageId) }
						}
						.addOnFailureListener { e ->
							if (item.senderId == receiverId)
								exec { callback.onFailure(e) }
							else
								Utils.logError(e)
						}
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	/*
			Unfortunatly, there is no way to send device2device messages avoiding Cloud Functions
	 */
	private fun sendNotification(notification: Notification) {

		db.collection(USERS)
			.document(notification.receiverId)
			.get()
			.addOnSuccessListener { snapshot ->
				snapshot.toObject(User::class.java)
					?.let { user ->
						exec {
							FirebaseMessaging.getInstance()
								.send(RemoteMessage.Builder(/*user.token + */"@gcm.googleapis.com")
									.setMessageId(notification.messageId)
									.addData("id", notification.messageId)
									.addData("chatId", notification.chatId)
									.addData("type", notification.type)
									.addData("title", notification.title)
									.addData("body", notification.text)
									.build())
						}
					}
			}
	}

	private data class Notification(val receiverId: String,
	                                val messageId: String,
	                                val chatId: String,
	                                val title: String,
	                                val text: String = "",
	                                val type: String = PushMessage.TYPE_UNKNOWN)
}
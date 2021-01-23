package com.ancientlore.intercom.data.source.remote.firestore

import android.util.Log
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.model.PushMessage
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.EmptyResultException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class FirestoreMessageNoCF(chatId: String): FirestoreMessageSource(chatId) {

	internal companion object  {
		private const val TAG = "FirestoreMessageNoCF"

		private const val CHATS = "chats"
		private const val USERS = "users"
	}

	private lateinit var chat: Chat

	init {
		db.collection(CHATS).document(chatId).get()
			.addOnSuccessListener { snapshot ->
				chat = snapshot.toObject(Chat::class.java)!!
			}
	}

	override fun addMessage(message: Message, callback: RequestCallback<String>?) {
		chatMessages.add(message)
			.addOnSuccessListener {
				if (chat == null) {
					callback?.onFailure(EmptyResultException("$TAG: empty"))
					return@addOnSuccessListener
				}
				chatMessages.document(it.id)
					.update(HashMap<String, Any>().apply {
						put("id", it.id)
						put("status", 1)
					})
					.addOnFailureListener { error -> Log.d(TAG, "Failure 1: ${error.message}") }

				val userChatInfoUpdate = HashMap<String, Any>().apply {
					put("lastMsgText", message.text)
					put("lastMsgTime", FieldValue.serverTimestamp())
				}

				if (chat.name.isEmpty()) {

					val senderId = message.senderId
					val receiverId = if (message.senderId != chat.participants[0]) chat.participants[0] else chat.participants[1]

					db.collection(USERS)
						.document(senderId)
						.collection(CHATS)
						.document(receiverId)
						.update(userChatInfoUpdate)
						.addOnFailureListener { error -> Log.d(TAG, "Failure 2: ${error.message}") }
					db.collection(USERS)
						.document(receiverId)
						.collection(CHATS)
						.document(senderId)
						.update(userChatInfoUpdate)
						.addOnFailureListener { error -> Log.d(TAG, "Failure 3: ${error.message}") }
				}
				else {
					for (receiverId in chat.participants) {
						db.collection(USERS)
							.document(receiverId)
							.collection(CHATS)
							.document(chat.name)
							.update(userChatInfoUpdate)
							.addOnFailureListener { error -> Log.d(TAG, "Failure 1: ${error.message}") }
					}
				}

				callback?.onSuccess(it.id)
			}
			.addOnFailureListener { callback?.onFailure(it) }
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

	private data class Notification(val receiverId: String,
	                                val messageId: String,
	                                val chatId: String,
	                                val title: String,
	                                val text: String = "",
	                                val type: String = PushMessage.TYPE_UNKNOWN)
}
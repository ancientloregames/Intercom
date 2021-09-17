package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Chat.Companion.TYPE_PRIVATE
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.ContactRepository
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_INITIATOR_ID
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_MUTE
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_PARTICIPANTS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_PIN
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_TYPE
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.firestore.SetOptions
import java.lang.RuntimeException
import kotlin.collections.HashMap

class FirestoreChatSourceNoCF(userId: String)
	: FirestoreChatSource(userId), ChatSource {

	override fun addItem(item: Chat, callback: RequestCallback<String>) {
		db.collection(CHATS)
			.document()
			.get()
			.addOnSuccessListener { doc ->

				val chatId = doc.id

				if (item.type == TYPE_PRIVATE) {
					val contactId = item.participants.first { it != item.initiatorId }
					ContactRepository.update(Contact(phone = contactId, chatId = chatId))

					db.collection(USERS)
						.document(item.participants[0])
						.collection(CHATS)
						.document(chatId)
						.set(hashMapOf(
							FIELD_ICON_URL to item.iconUrl,
							FIELD_NAME to item.participants[1],
							FIELD_PARTICIPANTS to item.participants,
							FIELD_TYPE to item.type,
							FIELD_PIN to item.pin,
							FIELD_MUTE to item.mute
						), SetOptions.merge())
						.addOnSuccessListener { exec { callback.onSuccess(chatId) } }
						.addOnFailureListener { exec { callback.onFailure(it) } }
					db.collection(USERS)
						.document(item.participants[1])
						.collection(CHATS)
						.document(chatId)
						.set(hashMapOf(
							FIELD_ICON_URL to item.iconUrl,
							FIELD_NAME to item.participants[0],
							FIELD_PARTICIPANTS to item.participants,
							FIELD_TYPE to item.type,
							FIELD_PIN to item.pin,
							FIELD_MUTE to item.mute
						), SetOptions.merge())
						.addOnFailureListener { exec { callback.onFailure(it) } }
				}
				else {
					val changeMap = hashMapOf(
						FIELD_NAME to item.name,
						FIELD_ICON_URL to item.iconUrl,
						FIELD_INITIATOR_ID to item.initiatorId,
						FIELD_PARTICIPANTS to item.participants,
						FIELD_TYPE to item.type,
						FIELD_PIN to item.pin,
						FIELD_MUTE to item.mute
					)

					for (participant in item.participants) {
						db.collection(USERS)
							.document(participant)
							.collection(CHATS)
							.document(chatId)
							.set(changeMap, SetOptions.merge())
							.addOnSuccessListener {
								if (participant == item.initiatorId)
									exec { callback.onSuccess(chatId) }
							}
							.addOnFailureListener {
								if (participant == item.initiatorId)
									exec { callback.onFailure(it) }
								else
									Utils.logError(it)
							}
					}
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {
		if (item.id.isNotEmpty()) {

			if (item.pin != null || item.mute != null) {

				val userChatChangeMap = HashMap<String, Any>().apply {
					if (item.pin != null)
						put(FIELD_PIN, item.pin!!)
					if (item.mute != null)
						put(FIELD_MUTE, item.mute!!)
				}
				userChats
					.document(item.id)
					.set(userChatChangeMap, SetOptions.merge())
					.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
					.addOnFailureListener { exec { callback.onFailure(it) } }
			}

			val changeMap = HashMap<String, Any>().apply {
				if (item.name.isNotEmpty() && item.type != TYPE_PRIVATE)
					put(FIELD_NAME, item.name)
				if (item.iconUrl.isNotEmpty())
					put(FIELD_ICON_URL, item.iconUrl)
				if (item.participants.isNotEmpty())
					put(FIELD_PARTICIPANTS, item.participants)
			}

			if (changeMap.size > 0) {
				db.collection(CHATS)
					.document(item.id)
					.set(changeMap, SetOptions.merge())
					.addOnSuccessListener {
						for (participant in item.participants) {
							db.collection(USERS)
								.document(participant)
								.collection(CHATS)
								.document(item.id)
								.set(changeMap, SetOptions.merge())
								.addOnSuccessListener {
									if (participant == item.initiatorId)
										exec { callback.onSuccess(EmptyObject) }
								}
								.addOnFailureListener {
									if (participant == item.initiatorId)
										exec { callback.onFailure(it) }
									else
										Utils.logError(it)
								}
						}
					}
					.addOnFailureListener { exec { callback.onFailure(it) } }
			}
		}
		else exec { callback.onFailure(RuntimeException("Chat id shouldn't be empty")) }
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		db.collection(CHATS)
			.document(id)
			.delete()
			.addOnSuccessListener {
				exec {
					db.collection(USERS)
						.document(userId)
						.collection(CHATS)
						.document(id)
						.delete()
						.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}
}
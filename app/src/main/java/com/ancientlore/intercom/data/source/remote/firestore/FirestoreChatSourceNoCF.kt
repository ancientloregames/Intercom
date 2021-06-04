package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Chat.Companion.TYPE_PRIVATE
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ID
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_MSG_TEXT
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_LAST_MSG_TIME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_PARTICIPANTS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_PIN
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_TYPE
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.utils.SingletonHolder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap

class FirestoreChatSourceNoCF private constructor(userId: String)
	: FirestoreChatSource(userId), ChatSource {

	internal companion object : SingletonHolder<FirestoreChatSourceNoCF, String>(
		{ userId -> FirestoreChatSourceNoCF(userId) }) {
		private const val TAG = "FirestoreChatSourceNoCF"
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		db.collection(CHATS).add(item)
			.addOnSuccessListener {

				val chatId = it.id

				if (item.type == TYPE_PRIVATE) {
					db.collection(USERS)
						.document(item.participants[0])
						.collection(CHATS)
						.document(item.participants[1])
						.set(hashMapOf(
							FIELD_ID to chatId,
							FIELD_ICON_URL to item.iconUrl,
							FIELD_LAST_MSG_TEXT to "",
							FIELD_LAST_MSG_TIME to Date(0),
							FIELD_NAME to item.participants[1],
							FIELD_TYPE to item.type,
							FIELD_PIN to item.pin
						), SetOptions.merge())
						.addOnSuccessListener { callback?.onSuccess(chatId) }
						.addOnFailureListener { error -> callback?.onFailure(error) }
					db.collection(USERS)
						.document(item.participants[1])
						.collection(CHATS)
						.document(item.participants[0])
						.set(hashMapOf(
							FIELD_ID to chatId,
							FIELD_ICON_URL to item.iconUrl,
							FIELD_LAST_MSG_TEXT to "",
							FIELD_LAST_MSG_TIME to Date(0),
							FIELD_NAME to item.participants[0],
							FIELD_TYPE to item.type,
							FIELD_PIN to item.pin
						), SetOptions.merge())
						.addOnFailureListener { error -> callback?.onFailure(error) }
				}
				else {
					for (participant in item.participants) {
						db.collection(USERS)
							.document(participant)
							.collection(CHATS)
							.document(chatId)
							.set(hashMapOf(
								FIELD_ID to chatId,
								FIELD_ICON_URL to item.iconUrl,
								FIELD_LAST_MSG_TEXT to "",
								FIELD_LAST_MSG_TIME to FieldValue.serverTimestamp(),
								FIELD_NAME to item.name,
								FIELD_TYPE to item.type,
								FIELD_PIN to item.pin
							), SetOptions.merge())
							.addOnSuccessListener {
								if (item.initiatorId == participant)
									callback?.onSuccess(chatId)
							}
							.addOnFailureListener { error -> callback?.onFailure(error) }
					}
				}
			}
			.addOnFailureListener { callback?.onFailure(it) }
	}

	//FIXME this looks ugly. Need to separate Chat and UserChat models
	override fun updateItem(item: Chat, callback: RequestCallback<Any>?) {
		if (item.id.isNotEmpty()) {

			if (item.pin != null) {

				val chatId = if (item.type == TYPE_PRIVATE) item.name else item.id

				val userChatChangeMap = HashMap<String, Any>().apply {
					if (item.pin != null)
						put(FIELD_PIN, item.pin)
				}
				userChats
					.document(chatId)
					.set(userChatChangeMap, SetOptions.merge())
					.addOnFailureListener { callback?.onFailure(it) }
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
						if (item.type == TYPE_PRIVATE) {
							db.collection(USERS)
								.document(item.participants[0])
								.collection(CHATS)
								.document(item.participants[1])
								.set(changeMap, SetOptions.merge())
								.addOnFailureListener { callback?.onFailure(it) }
							db.collection(USERS)
								.document(item.participants[1])
								.collection(CHATS)
								.document(item.participants[0])
								.set(changeMap, SetOptions.merge())
								.addOnFailureListener { callback?.onFailure(it) }
						}
						else {
							for (participant in item.participants) {
								db.collection(USERS)
									.document(participant)
									.collection(CHATS)
									.document(item.id)
									.set(changeMap, SetOptions.merge())
									.addOnFailureListener { callback?.onFailure(it) }
							}
						}

						callback?.onSuccess(EmptyObject)
					}
					.addOnFailureListener { callback?.onFailure(it) }
			}
		}
		else callback?.onFailure(RuntimeException("Chat id shouldn't be empty"))
	}
}
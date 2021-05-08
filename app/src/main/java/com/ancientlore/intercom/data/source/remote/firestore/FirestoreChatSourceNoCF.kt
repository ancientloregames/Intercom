package com.ancientlore.intercom.data.source.remote.firestore

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_ID
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_LAST_MSG_TEXT
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_LAST_MSG_TIME
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ICON_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_PARTICIPANTS
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.utils.SingletonHolder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.lang.RuntimeException

class FirestoreChatSourceNoCF private constructor(userId: String)
	: FirestoreChatSource(userId), ChatSource {

	internal companion object : SingletonHolder<FirestoreChatSourceNoCF, String>(
		{ userId -> FirestoreChatSourceNoCF(userId) }) {
		private const val TAG = "FirestoreChatSourceNoCF"
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		db.collection(CHATS).add(item)
			.addOnSuccessListener {
				if (item.name.isEmpty()) {
					db.collection(USERS)
						.document(item.participants[0])
						.collection(CHATS)
						.document(item.participants[1])
						.set(hashMapOf(
							CHAT_ID to it.id,
							FIELD_ICON_URL to item.iconUrl,
							CHAT_LAST_MSG_TEXT to "",
							CHAT_LAST_MSG_TIME to FieldValue.serverTimestamp(),
							CHAT_NAME to item.participants[1]
						), SetOptions.merge())
						.addOnFailureListener { error -> callback?.onFailure(error) }
					db.collection(USERS)
						.document(item.participants[1])
						.collection(CHATS)
						.document(item.participants[0])
						.set(hashMapOf(
							CHAT_ID to it.id,
							FIELD_ICON_URL to item.iconUrl,
							CHAT_LAST_MSG_TEXT to "",
							CHAT_LAST_MSG_TIME to FieldValue.serverTimestamp(),
							CHAT_NAME to item.participants[0]
						), SetOptions.merge())
						.addOnFailureListener { error -> callback?.onFailure(error) }
				}
				else {
					for (participant in item.participants) {
						db.collection(USERS)
							.document(participant)
							.collection(CHATS)
							.document(it.id)
							.set(hashMapOf(
								CHAT_ID to it.id,
								FIELD_ICON_URL to item.iconUrl,
								CHAT_LAST_MSG_TEXT to "",
								CHAT_LAST_MSG_TIME to FieldValue.serverTimestamp(),
								CHAT_NAME to item.name
							), SetOptions.merge())
							.addOnFailureListener { error -> callback?.onFailure(error) }
					}
				}

				callback?.onSuccess(it.id)
			}
			.addOnFailureListener { callback?.onFailure(it) }
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>?) {
		if (item.id.isNotEmpty()) {

			db.collection(CHATS)
				.document(item.id)
				.set(HashMap<String, Any>().apply {
					if (item.name.isNotEmpty())
						put(FIELD_NAME, item.name)
					if (item.iconUrl.isNotEmpty())
						put(FIELD_ICON_URL, item.iconUrl)
					if (item.participants.isNotEmpty())
						put(FIELD_PARTICIPANTS, item.participants)
				}, SetOptions.merge())
				.addOnSuccessListener {
					for (participant in item.participants) {
						db.collection(USERS)
							.document(participant)
							.collection(CHATS)
							.document(item.id)
							.set(HashMap<String, Any>().apply {
								if (item.name.isNotEmpty())
									put(FIELD_NAME, item.name)
								if (item.iconUrl.isNotEmpty())
									put(FIELD_ICON_URL, item.iconUrl)
								if (item.participants.isNotEmpty())
									put(FIELD_PARTICIPANTS, item.participants)
							}, SetOptions.merge())
							.addOnFailureListener { callback?.onFailure(it) }
					}
					callback?.onSuccess(EmptyObject)
				}
				.addOnFailureListener { callback?.onFailure(it) }
		}
		else callback?.onFailure(RuntimeException("Chat id shouldn't be empty"))
	}
}
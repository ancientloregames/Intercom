package com.ancientlore.intercom.data.source.remote.firestore

import android.util.Log
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_ID
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_LAST_MSG_TEXT
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_LAST_MSG_TIME
import com.ancientlore.intercom.data.source.remote.firestore.C.CHAT_NAME
import com.ancientlore.intercom.data.source.remote.firestore.C.USERS
import com.ancientlore.intercom.utils.SingletonHolder
import java.util.*

class FirestoreChatSourceNoCF private constructor(userId: String)
	: FirestoreChatSource(userId), ChatSource {

	internal companion object : SingletonHolder<FirestoreChatSourceNoCF, String>(
		{ userId -> FirestoreChatSourceNoCF(userId) }) {
		private const val TAG = "FirestoreChatSourceNoCF"
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>?) {
		db.collection(CHATS).add(item)
			.addOnSuccessListener {
				val initialDate = Date(0)

				if (item.name.isEmpty()) {
					db.collection(USERS)
						.document(item.participants[0])
						.collection(CHATS)
						.document(item.participants[1])
						.set(hashMapOf(
							CHAT_ID to it.id,
							CHAT_LAST_MSG_TEXT to "",
							CHAT_LAST_MSG_TIME to initialDate,
							CHAT_NAME to item.participants[1]
						))
						.addOnFailureListener { error -> Log.d(TAG, "Failure 1: ${error.message}") }
					db.collection(USERS)
						.document(item.participants[1])
						.collection(CHATS)
						.document(item.participants[0])
						.set(hashMapOf(
							CHAT_ID to it.id,
							CHAT_LAST_MSG_TEXT to "",
							CHAT_LAST_MSG_TIME to initialDate,
							CHAT_NAME to item.participants[0]
						))
						.addOnFailureListener { error -> Log.d(TAG, "Failure 2: ${error.message}") }
				}
				else {
					for (participant in item.participants) {
						db.collection(USERS)
							.document(participant)
							.collection(CHATS)
							.document(it.id)
							.set(hashMapOf(
								CHAT_ID to it.id,
								CHAT_LAST_MSG_TEXT to "",
								CHAT_LAST_MSG_TIME to initialDate,
								CHAT_NAME to item.name
							))
					}
				}

				callback?.onSuccess(it.id)
			}
			.addOnFailureListener { callback?.onFailure(it) }
	}
}
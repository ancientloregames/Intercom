package com.ancientlore.intercom.data.source.remote.firestore

import android.net.Uri
import android.util.Log
import com.ancientlore.intercom.C
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.ListChanges
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.data.source.remote.firestore.C.CHATS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_ATTACH_URL
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_STATUS
import com.ancientlore.intercom.data.source.remote.firestore.C.FIELD_TIMESTAMP
import com.ancientlore.intercom.data.source.remote.firestore.C.MESSAGES
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList

open class FirestoreMessageSource(private val chatId: String)
	: FirestoreSource<Message>(), MessageSource {

	protected val chatMessages get() = db.collection(CHATS).document(chatId).collection(MESSAGES)

	private var paginationLimit: Long = C.DEF_MSG_PAGINATION_LIMIT

	private var lastDocument: DocumentSnapshot? = null

	@Volatile
	private var paginationCompleted = false

	override fun getObjectClass() = Message::class.java

	override fun getWorkerThreadName() = "fsMessageSource_thread"

	override fun getSourceId() = chatId

	override fun getAll(callback: RequestCallback<List<Message>>) {
		chatMessages
			.get()
			.addOnSuccessListener { exec { callback.onSuccess(deserialize(it)) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getNextPage(callback: RequestCallback<List<Message>>) {

		if (paginationCompleted) {
			callback.onSuccess(emptyList())
			return
		}

		val queryNextPage = if (lastDocument != null) {
			chatMessages
				.orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
				.startAfter(lastDocument!!)
				.limit(paginationLimit)
		}
		else {
			chatMessages
				.orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
				.limit(paginationLimit)
		}

		queryNextPage
			.get()
			.addOnSuccessListener { snapshot ->
				exec {
					if (snapshot.isEmpty.not()) {
						lastDocument = snapshot.documents[snapshot.size() - 1]
						callback.onSuccess(deserialize(snapshot))
					}
					else {
						paginationCompleted = true
						lastDocument = null
						callback.onSuccess(emptyList())
					}
				}
			}
			.addOnFailureListener { callback.onFailure(it) }
	}

	override fun setPaginationLimit(limit: Long) {
		if (limit > 1)
			this.paginationLimit = limit
		else
			Utils.logError("Pagination limit must be > 1")
	}

	override fun getItem(id: String, callback: RequestCallback<Message>) {

		chatMessages
			.document(id)
			.get()
			.addOnSuccessListener { snapshot ->
				exec {
					deserialize(snapshot)
						?.let { callback.onSuccess(it) }
						?: callback.onFailure(EmptyResultException)
				}
			}
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addItem(item: Message, callback: RequestCallback<String>) {
		chatMessages
			.add(item)
			.addOnSuccessListener { exec { callback.onSuccess(it.id) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun addItems(items: List<Message>, callback: RequestCallback<List<String>>) {
		if (items.isEmpty()) {
			callback.onSuccess(emptyList())
			return
		}
		val serverIds = ArrayList<String>(items.size)
		val lastMessageId = items.last().id
		for (item in items) {
			chatMessages
				.add(item)
				.addOnSuccessListener {
					serverIds.add(it.id)
					if (item.id == lastMessageId) {
						exec { callback.onSuccess(serverIds) }
					}
				}
				.addOnFailureListener { exec { callback.onFailure(it) } }
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		chatMessages
			.document(id)
			.delete()
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {
		chatMessages
			.document(messageId)
			.update(FIELD_ATTACH_URL, uri.toString())
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {
		chatMessages
			.document(id)
			.update(FIELD_STATUS, Message.STATUS_RECEIVED)
			.addOnSuccessListener { exec { callback.onSuccess(EmptyObject) } }
			.addOnFailureListener { exec { callback.onFailure(it) } }
	}

	override fun attachListener(callback: RequestCallback<List<Message>>) : RepositorySubscription {
		val registration = chatMessages
			.orderBy(FIELD_TIMESTAMP)
			.addSnapshotListener { snapshot, error ->
				exec {
					when {
						error != null -> callback.onFailure(error)
						snapshot != null -> callback.onSuccess(deserialize(snapshot))
						else -> callback.onFailure(EmptyResultException)
					}
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				registration.remove()
			}
		}
	}

	override fun attachChangeListener(callback: RequestCallback<ListChanges<Message>>) : RepositorySubscription {
		val registration = chatMessages
			.orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
			.limit(paginationLimit)
			.addSnapshotListener { snapshot, error ->
				exec {
					when {
						error != null -> callback.onFailure(error)
						snapshot != null -> {

							val limitIndex = paginationLimit.toInt() - 1

							val addList = LinkedList<Message>()
							val modifyList = LinkedList<Message>()
							val removeList = LinkedList<Message>()

							for (change in snapshot.documentChanges) {
								// Only the real changes
								val document = change.document
								when (change.type) {
									DocumentChange.Type.ADDED -> {
										// Ignore the addition via query limit and cache (first call)
										if (change.newIndex != limitIndex && document.metadata.isFromCache.not()) {
											val messageToAdd = deserialize(document)
											// ensure id and timestamp (pending addition comes without them)
											if (messageToAdd.id.isEmpty())
												messageToAdd.id = document.id
											if (messageToAdd.timestamp == null)
												messageToAdd.timestamp = Date(System.currentTimeMillis())
											addList.add(messageToAdd)
										}
									}
									DocumentChange.Type.REMOVED -> {
										// Ignore the deletion via query limit
										if (change.oldIndex != limitIndex)
											removeList.add(deserialize(document))
									}
									DocumentChange.Type.MODIFIED -> {
										val messageToModify = deserialize(document)
										// ensure id and timestamp
										if (messageToModify.id.isEmpty())
											messageToModify.id = document.id
										if (messageToModify.timestamp == null)
											messageToModify.timestamp = Date(System.currentTimeMillis())
										modifyList.add(messageToModify)
									}
								}
							}

							if (addList.isNotEmpty() || modifyList.isNotEmpty() || removeList.isNotEmpty()) {
								Log.d(C.DEFAULT_LOG_TAG, "OnMessageListChange:" +
										"\n\raddList: ${addList.size}" +
										"\n\rremoveList: ${removeList.size}" +
										"\n\rupdateList: ${modifyList.size}")
								callback.onSuccess(ListChanges(addList, modifyList, removeList))
							}
						}
						else -> callback.onFailure(EmptyResultException)
					}
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				registration.remove()
			}
		}
	}

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {
		TODO("Not yet implemented")
	}
}
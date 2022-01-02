package com.ancientlore.intercom.data.source.test

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.EmptyResultException
import java.util.*
import kotlin.collections.HashMap

abstract class TestChatSource: TestSource(), ChatSource {

	companion object {

		private const val chatSourceSize = 100

		val testChatData: HashMap<String, Chat> by lazy { HashMap<String, Chat>(chatSourceSize).apply {

			val random = Random()

			val currentTime = System.currentTimeMillis()

			for (i in 0..chatSourceSize) {

				val initialorId = testUserIds[random.nextInt(userListSize)]

				val type = if (random.nextBoolean()) Chat.TYPE_PRIVATE else Chat.TYPE_GROUP

				val otherUserIds = testUserIds.minus(initialorId)

				val participants = if (type == Chat.TYPE_PRIVATE) {
					val collucutorId = otherUserIds.random()
					listOf(initialorId, collucutorId)
				}
				else {
					listOf(initialorId)
						.plus(otherUserIds
							.shuffled()
							.take(random.nextInt(userListSize - 2) + 2)
							.toList())
				}

				val lastMsgSenderId = participants.random()

				val id = i.toString()
				put(id, Chat(
					id = id,
					name = "chat $id",
					initiatorId = initialorId,
					participants = participants,
					lastMsgSenderId = lastMsgSenderId,
					lastMsgTime = Date(random.nextLong() % currentTime),
					lastMsgText = "lastMsgText $lastMsgSenderId",
					type = type,
					pin = random.nextBoolean(),
					mute = random.nextBoolean(),
					userId = testCurrentUserId
				))
			}
		} }
	}

	override fun getSourceId() = testCurrentUserId

	override fun getAll(callback: RequestCallback<List<Chat>>) {
		callback.onSuccess(testChatData.values.toList())
	}

	override fun getItem(id: String, callback: RequestCallback<Chat>) {
		testChatData[id]
			?.let { callback.onSuccess(it) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: Chat, callback: RequestCallback<String>) {
		val id = (testChatData.size + 1).toString()
		testChatData[id] = item
		callback.onSuccess(id)
	}

	override fun addItems(items: List<Chat>, callback: RequestCallback<List<String>>) {
		val ids = ArrayList<String>(items.size)
		for (item in items) {
			val id = (testChatData.size + 1).toString()
			testChatData[id] = item
			ids.add(id)
		}
		callback.onSuccess(ids)
	}

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {
		testChatData[item.id]
			?.let {
				if (item.name.isNotEmpty())
					it.name = item.name
				if (item.iconUrl.isNotEmpty())
					it.iconUrl = item.iconUrl
				if (item.newMsgCount > 0)
					it.newMsgCount = item.newMsgCount
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun setMessageRecieved(id: String, callback: RequestCallback<Any>) {
		testChatData[id]
			?.let {
				it.newMsgCount = 0
			}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		testChatData.remove(id)
			?.let { callback.onSuccess(EmptyObject) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun attachListener(callback: RequestCallback<List<Chat>>): RepositorySubscription {
		getAll(callback) // TODO simulate with limits in future

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<Chat>): RepositorySubscription {
		getItem(id, callback)

		return DummyRepositorySubscription
	}

	override fun getBroadcasts(callback: RequestCallback<List<Chat>>) {
		TODO("Not yet implemented")
	}
}
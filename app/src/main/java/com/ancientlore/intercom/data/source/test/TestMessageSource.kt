package com.ancientlore.intercom.data.source.test

import android.net.Uri
import com.ancientlore.intercom.C
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.ListChanges
import com.ancientlore.intercom.data.source.MessageSource
import java.util.*
import kotlin.collections.HashMap

abstract class TestMessageSource: TestSource(), MessageSource {

	companion object {

		private const val messageSourceSize = 100

		val testMessageData: HashMap<String, Message> by lazy { HashMap<String, Message>(messageSourceSize).apply {

			val currentTime = System.currentTimeMillis()

			val random = Random()

			for (i in 0..messageSourceSize) {
				val id = i.toString()

				val receivers = testUserIds
					.shuffled()
					.take(random.nextInt(userListSize - 2) + 2)
					.toList()

				put(id, Message(
					id = id,
					timestamp = Date(random.nextLong() % currentTime),
					senderId = receivers.random(),
					text = "Text $id",
					info = "Info $id",
					type = Message.TYPE_TEXT, // TODO randomize
					status = random.nextInt() % 3,
					progress = random.nextInt(100),
					chatId = id,
					localId = i.toLong(),
					receivers = receivers
				))
			}
		} }
	}

	private var paginationLimit = C.DEF_MSG_PAGINATION_LIMIT.toInt()
	private var pageOffset = 0
	private var paginationCompleted = false

	override fun getSourceId() = testCurrentUserId

	override fun getAll(callback: RequestCallback<List<Message>>) {

		callback.onSuccess(testMessageData.values.toList())
	}

	override fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>) {

		val result = testMessageData.filter { ids.contains(it.key) }.values.toList()
		if (result.isNotEmpty())
			callback.onSuccess(result)
		else
			callback.onFailure(EmptyResultException)
	}

	override fun getItem(id: String, callback: RequestCallback<Message>) {

		testMessageData[id]
			?.let { callback.onSuccess(it) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun getNextPage(callback: RequestCallback<List<Message>>) {

		if (paginationCompleted) {
			callback.onSuccess(emptyList())
			return
		}

		val prevOffset = pageOffset
		pageOffset += paginationLimit

		val page = if (pageOffset >= testMessageData.size) {
			paginationCompleted = true
			testMessageData.values.toList().subList(prevOffset, testMessageData.size)
		}
		else
			testMessageData.values.toList().subList(prevOffset, pageOffset)

		callback.onSuccess(page)
	}

	override fun addItem(item: Message, callback: RequestCallback<String>) {

		val id = (testMessageData.size + 1).toString()
		testMessageData[id] = item
		callback.onSuccess(id)
	}

	override fun addItems(items: List<Message>, callback: RequestCallback<List<String>>) {

		val ids = ArrayList<String>(items.size)
		for (item in items) {
			val id = (testMessageData.size + 1).toString()
			testMessageData[id] = item
			ids.add(id)
		}
		callback.onSuccess(ids)
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {

		testMessageData.remove(id)
			?.let { callback.onSuccess(EmptyObject) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun updateMessageUri(messageId: String, uri: Uri, callback: RequestCallback<Any>) {

		testMessageData[messageId]
			?.let {
				it.attachUrl = uri.toString()
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {

		testMessageData[id]
			?.let {
				it.status = Message.STATUS_RECEIVED
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun setPaginationLimit(limit: Long) {

		if (limit > 1)
			paginationLimit = limit.toInt()
	}

	override fun attachListener(callback: RequestCallback<List<Message>>): RepositorySubscription {

		getAll(callback) // TODO simulate with limits in future

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {

		getItem(id, callback)

		return DummyRepositorySubscription
	}

	override fun attachChangeListener(callback: RequestCallback<ListChanges<Message>>): RepositorySubscription {

		return DummyRepositorySubscription
	}
}
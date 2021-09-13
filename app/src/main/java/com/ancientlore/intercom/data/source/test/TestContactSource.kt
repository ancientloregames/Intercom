package com.ancientlore.intercom.data.source.test

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.EmptyResultException
import java.util.*

abstract class TestContactSource: TestSource(), ContactSource {

	companion object {

		private const val contactSourceSize = userListSize - 1

		val testContactSource: HashMap<String, Contact> by lazy { HashMap<String, Contact>(contactSourceSize).apply {

			val testContactIds = testUserIds.minus(testCurrentUserId)
			for (i in 0..contactSourceSize) {
				val phone = testContactIds[i]
				val name = "user_$phone"
				put(phone, Contact(
					phone = phone,
					name = name,
					chatId = i.toString(),
					userId = testCurrentUserId
				))
			}
		} }
	}

	override fun getSourceId() = testCurrentUserId

	override fun getAll(callback: RequestCallback<List<Contact>>) {

		callback.onSuccess(testContactSource.values.toList())
	}

	override fun getItem(id: String, callback: RequestCallback<Contact>) {

		testContactSource[id]
			?.let { callback.onSuccess(it) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun getItems(ids: List<String>, callback: RequestCallback<List<Contact>>) {

		val result = testContactSource.filter { ids.contains(it.key) }.values.toList()

		if (result.isNotEmpty())
			callback.onSuccess(result)
		else
			callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: Contact, callback: RequestCallback<String>) {

		val id = (testContactSource.size + 1).toString()
		testContactSource[id] = item
		callback.onSuccess(id)
	}

	override fun addItems(items: List<Contact>, callback: RequestCallback<List<String>>) {

		val ids = ArrayList<String>(items.size)
		for (item in items) {
			val id = (testContactSource.size + 1).toString()
			testContactSource[id] = item
			ids.add(id)
		}
		callback.onSuccess(ids)
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {

		testContactSource.remove(id)
			?.let { callback.onSuccess(EmptyObject) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun update(item: Contact, callback: RequestCallback<Any>) {

		testContactSource[item.phone]
			?.let {
				if (item.name.isNotEmpty())
					it.name = item.name
				if (item.iconUrl.isNotEmpty())
					it.iconUrl = item.iconUrl
				if (item.chatId.isNotEmpty())
					it.chatId = item.chatId
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun update(items: List<Contact>, callback: RequestCallback<Any>) {

		var success = true
		for (item in items) {
			testContactSource[item.phone]
				?.let {
					if (item.name.isNotEmpty())
						it.name = item.name
					if (item.iconUrl.isNotEmpty())
						it.iconUrl = item.iconUrl
					if (item.chatId.isNotEmpty())
						it.chatId = item.chatId
				}
				?:let { success = false }
		}
		if (success)
			callback.onSuccess(EmptyObject)
		else
			callback.onFailure(EmptyResultException)
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>): RepositorySubscription {

		getAll(callback) // TODO simulate with limits in future

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>): RepositorySubscription {

		getItem(id, callback)

		return DummyRepositorySubscription
	}
}
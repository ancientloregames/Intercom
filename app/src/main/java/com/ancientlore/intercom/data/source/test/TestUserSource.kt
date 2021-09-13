package com.ancientlore.intercom.data.source.test

import android.net.Uri
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.data.source.EmptyResultException
import com.ancientlore.intercom.data.source.UserSource
import java.util.*
import kotlin.collections.HashMap

abstract class TestUserSource: TestSource(), UserSource {

	companion object {

		val testUserSource: HashMap<String, User> by lazy { HashMap<String, User>(userListSize).apply {

			val random = Random()

			val currentTime = System.currentTimeMillis()

			for (i in 0..userListSize) {
				val phone = testUserIds[i]
				val name = "user_$phone"
				put(phone, User(
					name = name,
					phone = phone,
					email = "$name@mail.com",
					status = "$name status",
					lastSeenTime = Date(random.nextLong() % currentTime),
					online = random.nextBoolean(),
					token = "${name}_${random.nextInt()}",
				))
			}
		} }
	}

	override fun getSourceId() = testCurrentUserId

	override fun getAll(callback: RequestCallback<List<User>>) {

		callback.onSuccess(testUserSource.values.toList())
	}

	override fun getItem(id: String, callback: RequestCallback<User>) {

		testUserSource[id]
			?.let { callback.onSuccess(it) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun addItem(item: User, callback: RequestCallback<String>) {

		val id = (testUserSource.size + 1).toString()
		testUserSource[id] = item
		callback.onSuccess(id)
	}

	override fun addItems(items: List<User>, callback: RequestCallback<List<String>>) {

		val ids = ArrayList<String>(items.size)
		for (item in items) {
			val id = (testUserSource.size + 1).toString()
			testUserSource[id] = item
			ids.add(id)
		}
		callback.onSuccess(ids)
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {

		testUserSource.remove(id)
			?.let { callback.onSuccess(EmptyObject) }
			?: callback.onFailure(EmptyResultException)
	}

	override fun updateNotificationToken(token: String, callback: RequestCallback<Any>) {

		testUserSource[testCurrentUserId]
			?.let {
				it.token = token
				callback.onSuccess(it)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun updateIcon(uri: Uri, callback: RequestCallback<Any>) {

		testUserSource[testCurrentUserId]
			?.let {
				it.iconUrl = uri.toString()
				callback.onSuccess(it)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun updateName(name: String, callback: RequestCallback<Any>) {

		testUserSource[testCurrentUserId]
			?.let {
				it.name = name
				callback.onSuccess(it)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun updateStatus(status: String, callback: RequestCallback<Any>) {

		testUserSource[testCurrentUserId]
			?.let {
				it.status = status
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun updateOnlineStatus(online: Boolean, callback: RequestCallback<Any>) {

		testUserSource[testCurrentUserId]
			?.let {
				it.online = online
				callback.onSuccess(it)
			}
			?: callback.onFailure(EmptyResultException)
	}

	override fun attachListener(callback: RequestCallback<List<User>>): RepositorySubscription {

		getAll(callback) // TODO simulate with limits in future

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<User>): RepositorySubscription {

		getItem(id, callback)

		return DummyRepositorySubscription
	}
}
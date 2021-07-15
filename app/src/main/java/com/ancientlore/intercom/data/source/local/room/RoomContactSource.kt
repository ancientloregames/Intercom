package com.ancientlore.intercom.data.source.local.room

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.EmptyResultException

class RoomContactSource(private val userId: String,
                        private val dao: RoomContactDao) : RoomSource(), ContactSource {

	override fun getWorkerThreadName() = "roomContactSource_thread"

	override fun getSourceId() = userId

	override fun getAll(callback: RequestCallback<List<Contact>>) {

		exec {
			val items = dao.getAll(userId)
			if (items.isNotEmpty())
				callback.onSuccess(items)
			else
				callback.onFailure(EmptyResultException)
		}
	}

	override fun getItem(id: String, callback: RequestCallback<Contact>) {

		exec {
			dao.getById(id, userId)
				?.let { callback.onSuccess(it) }
				?: callback.onFailure(EmptyResultException)
		}
	}

	override fun addItem(item: Contact, callback: RequestCallback<String>) {

		exec {
			item.userId = userId
			dao.insert(item)
		}
	}

	override fun addItems(items: List<Contact>, callback: RequestCallback<List<String>>) {

		exec {
			for (item in items)
				item.userId = userId
			dao.insert(items)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		TODO("Not yet implemented")
	}

	override fun update(items: List<Contact>, callback: RequestCallback<Any>) {

		exec {
			for (item in items)
				item.userId = userId
			dao.insert(items)
		}
	}

	override fun update(item: Contact, callback: RequestCallback<Any>) {

		exec {
			if (item.name.isNotEmpty())
				dao.updateName(item.phone, item.name)
			if (item.iconUrl.isNotEmpty())
				dao.updateIconUrl(item.phone, item.iconUrl)
		}
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>): RepositorySubscription {
		TODO("Not yet implemented")
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>): RepositorySubscription {
		TODO("Not yet implemented")
	}
}
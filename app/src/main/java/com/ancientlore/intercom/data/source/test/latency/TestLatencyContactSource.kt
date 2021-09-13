package com.ancientlore.intercom.data.source.test.latency

import com.ancientlore.intercom.backend.DummyRepositorySubscription
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.source.test.TestContactSource

abstract class TestLatencyContactSource: TestContactSource(), LatencySource {

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		schedule {
		}
		super.getAll(callback)
	}

	override fun getItem(id: String, callback: RequestCallback<Contact>) {
		schedule {
			super.getItem(id, callback)
		}
	}

	override fun getItems(ids: List<String>, callback: RequestCallback<List<Contact>>) {
		schedule {
			super.getItems(ids, callback)
		}
	}

	override fun addItem(item: Contact, callback: RequestCallback<String>) {
		schedule {
			super.addItem(item, callback)
		}
	}

	override fun addItems(items: List<Contact>, callback: RequestCallback<List<String>>) {
		schedule {
			super.addItems(items, callback)
		}
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {
		schedule {
			super.deleteItem(id, callback)
		}
	}

	override fun update(item: Contact, callback: RequestCallback<Any>) {
		schedule {
			super.update(item, callback)
		}
	}

	override fun update(items: List<Contact>, callback: RequestCallback<Any>) {
		schedule {
			super.update(items, callback)
		}
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>): RepositorySubscription {
		schedule {
			super.attachListener(callback)
		}

		return DummyRepositorySubscription
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>): RepositorySubscription {
		schedule {
			super.attachListener(id, callback)
		}

		return DummyRepositorySubscription
	}
}
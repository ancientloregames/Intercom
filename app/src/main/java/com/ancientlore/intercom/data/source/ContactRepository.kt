package com.ancientlore.intercom.data.source

import android.util.Log
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Contact

object ContactRepository : ContactSource { //TODO Cache Source usage

	private var remoteSource: ContactSource? = null

	override fun getAll(callback: RequestCallback<List<Contact>>) {
		remoteSource?.getAll(callback)
	}

	override fun addAll(contacts: List<Contact>, callback: RequestCallback<Any>) {
		remoteSource?.addAll(contacts, callback)
	}

	override fun update(contacts: List<Contact>, callback: RequestCallback<Any>?) {
		remoteSource?.update(contacts, callback)
	}

	override fun attachListener(callback: RequestCallback<List<Contact>>) : RepositorySubscription {
		return remoteSource
			?.attachListener(callback)
			?: object : RepositorySubscription {
				override fun remove() {
					Log.w("ContactRepository",
						"attachChangeListener(): There were no remoteSource! No subscription to remove")
				}
			}
	}

	override fun attachListener(id: String, callback: RequestCallback<Contact>) : RepositorySubscription {
		return remoteSource
			?.attachListener(id, callback)
			?: object : RepositorySubscription {
				override fun remove() {
					Log.w("ContactRepository",
						"attachContactListener(): There were no remoteSource! No subscription to remove")
				}
			}
	}

	fun setRemoteSource(source: ContactSource) {
		remoteSource = source
	}
}
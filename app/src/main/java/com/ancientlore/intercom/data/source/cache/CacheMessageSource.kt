package com.ancientlore.intercom.data.source.cache

import android.net.Uri
import com.ancientlore.intercom.C
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.utils.Utils

object CacheMessageSource : CacheSource<String, Message>() {

	private var paginationLimit: Long = C.DEF_MSG_PAGINATION_LIMIT
	private var currentPageOffset: Long = 0

	private var paginationCompleted: Boolean = false

	fun getNextPage(callback: RequestCallback<List<Message>>) {
		if (paginationCompleted) {
			return
		}

		// TODO need index of cache values by timestamp (desc) and separate thread
	}

	fun updateMessageUri(id: String, uri: Uri) {

		cache[id]?.attachUrl = uri.toString()
	}

	fun setMessageStatusReceived(id: String) {

		cache[id]?.status = Message.STATUS_RECEIVED
	}

	fun setPaginationLimit(limit: Long) {
		if (limit > 1) {
			this.paginationLimit = limit
		}
		else Utils.logError("Pagination limit must be > 1")
	}

	override fun clean() {
		paginationLimit = C.DEF_MSG_PAGINATION_LIMIT
		currentPageOffset = 0
		paginationCompleted = false
		super.clean()
	}
}
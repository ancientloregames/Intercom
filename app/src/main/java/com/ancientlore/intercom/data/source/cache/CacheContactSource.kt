package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.Contact

object CacheContactSource : CacheSource<Contact>() {

	fun addItems(items: List<Contact>) {
		items.forEach {
			cache[it.id] = it
		}
	}

	fun updateItem(item: Contact) {
		cache[item.id]?.let {
			cache[item.id] = Contact(
				it.phone,
				if (item.name.isNotEmpty()) item.name else it.name,
				item.chatId,
				if (item.iconUrl.isNotEmpty()) item.iconUrl else it.iconUrl,
				if (item.lastSeenTime > 0L) item.lastSeenTime else it.lastSeenTime
			)
		}
	}

	fun updateItems(items: List<Contact>) {
		for (item in items) {
			updateItem(item)
		}
	}

	fun reset(newChats: List<Contact>) {
		cache.clear()
		newChats.forEach {
			cache[it.id] = it
		}
	}
}
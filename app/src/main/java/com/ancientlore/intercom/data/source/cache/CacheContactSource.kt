package com.ancientlore.intercom.data.source.cache

import com.ancientlore.intercom.data.model.Contact

object CacheContactSource : CacheSource<String, Contact>() {

	fun updateItem(item: Contact) {
		cache[item.getIdentity()]?.let {
			if (item.name.isNotEmpty())
				it.name = item.name
			if (item.iconUrl.isNotEmpty())
				it.iconUrl = item.iconUrl
		}
	}

	fun updateItems(items: List<Contact>) {
		for (item in items) {
			updateItem(item)
		}
	}
}
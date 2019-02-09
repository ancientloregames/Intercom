package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.data.model.Contact

interface ContactSource : DataSource<Contact> {
	fun addAll(contacts: List<Contact>)
}
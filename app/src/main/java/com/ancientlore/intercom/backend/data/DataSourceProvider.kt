package com.ancientlore.intercom.backend.data

import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.MessageSource

interface DataSourceProvider {

	fun getChatSource(): ChatSource
	fun getMessageSource(): MessageSource
	fun getContactSource(): ContactSource

}
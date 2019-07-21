package com.ancientlore.intercom.backend

import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.MessageSource

interface DataSourceProvider {

	fun getChatSource(userId: String): ChatSource
	fun getMessageSource(chatId: String): MessageSource
	fun getContactSource(userId: String): ContactSource

}
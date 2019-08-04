package com.ancientlore.intercom.backend

import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.ContactSource
import com.ancientlore.intercom.data.source.MessageSource
import com.ancientlore.intercom.data.source.UserSource

interface DataSourceProvider {
	fun getUserSource(userId: String): UserSource
	fun getChatSource(userId: String): ChatSource
	fun getMessageSource(chatId: String): MessageSource
	fun getContactSource(userId: String): ContactSource
}
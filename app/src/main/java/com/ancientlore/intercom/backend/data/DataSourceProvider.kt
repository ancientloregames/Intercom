package com.ancientlore.intercom.backend.data

import com.ancientlore.intercom.data.source.ChatSource
import com.ancientlore.intercom.data.source.MessageSource

abstract class DataSourceProvider {

	abstract fun getChatSource(): ChatSource
	abstract fun getMessageSource(): MessageSource
}
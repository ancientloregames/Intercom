package com.ancientlore.intercom.frontend

import com.ancientlore.intercom.backend.DataSourceProvider

interface FrontendFactory {
	fun getDataSourceProvider(): DataSourceProvider
}
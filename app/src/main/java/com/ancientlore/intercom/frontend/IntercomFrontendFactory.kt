package com.ancientlore.intercom.frontend

import android.content.Context

class IntercomFrontendFactory(private val appContext: Context) : FrontendFactory {

	override fun getDataSourceProvider() = RoomDataSourceProvider(appContext)
}
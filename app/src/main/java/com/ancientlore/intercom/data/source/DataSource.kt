package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.backend.RequestCallback

interface DataSource<T> {

	fun getAll(callback: RequestCallback<List<T>>)

}
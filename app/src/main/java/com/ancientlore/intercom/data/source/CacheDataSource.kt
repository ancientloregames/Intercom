package com.ancientlore.intercom.data.source

interface LocalDataSource<T>
fun getAll(callback: RequestCallback<List<T>>) {
}
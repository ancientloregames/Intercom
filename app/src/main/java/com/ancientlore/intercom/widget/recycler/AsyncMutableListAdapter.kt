package com.ancientlore.intercom.widget.recycler

import io.reactivex.Single

interface AsyncMutableListAdapter<T> {
	fun setItems(newItems: List<T>): Single<Boolean>
	fun setItem(newItem: T, position: Int): Single<Boolean>
	fun prependItem(newItem: T): Single<Boolean>
	fun prependItems(newItems: List<T>): Single<Boolean>
	fun appendItem(newItem: T): Single<Boolean>
	fun appendItems(newItems: List<T>): Single<Boolean>
	fun updateItem(updatedItem: T): Single<Boolean>
	fun deleteItem(itemToDelete: T): Single<Boolean>
}
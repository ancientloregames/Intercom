package com.ancientlore.intercom.ui

interface MutableAdapter<T> {
	fun setItems(newItems: List<T>)
	fun setItem(newItem: T, position: Int): Boolean
	fun prependItem(newItem: T): Boolean
	fun appendItem(newItem: T): Boolean
	fun updateItem(updatedItem: T): Boolean
	fun deleteItem(itemToDelete: T): Boolean
	fun deleteItem(id: Long): Boolean
}
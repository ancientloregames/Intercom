package com.ancientlore.intercom.widget

interface MutableAdapter<T> {
	fun setItems(newItems: List<T>)
	fun setItem(newItem: T, position: Int): Boolean
	fun prependItem(newItem: T): Boolean
	fun prependItems(newItems: List<T>): Boolean
	fun appendItem(newItem: T): Boolean
	fun appendItems(newItems: List<T>): Boolean
	fun updateItem(updatedItem: T): Boolean
	fun deleteItem(itemToDelete: T): Boolean
}
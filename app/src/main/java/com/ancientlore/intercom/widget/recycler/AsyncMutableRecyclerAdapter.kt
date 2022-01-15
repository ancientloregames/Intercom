package com.ancientlore.intercom.widget.recycler

import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.utils.LoggingThreadFactory
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executors

abstract class AsyncMutableRecyclerAdapter<I: Comparable<I>, H: BasicRecyclerAdapter.ViewHolder<I, B>, B: ViewDataBinding>(
	items: MutableList<I> = mutableListOf(),
	withHeader: Boolean = false,
	withFooter: Boolean = false,
	protected var autoSort: Boolean = false)
	: FilterableRecyclerAdapter<I, H, B>(items, withHeader, withFooter),
	AsyncMutableListAdapter<I> {

	private val executor = Executors.newSingleThreadExecutor(LoggingThreadFactory("mutableListAdapter_thread"))

	protected fun runAsync(command: Runnable) {
		executor.execute(command)
	}

	override fun setItems(newItems: List<I>): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {

				val items =
					if (autoSort) newItems.sorted()
					else newItems

				fullList.clear()
				fullList.addAll(items)

				val diffResult = HeadedRecyclerDiffUtil.calculateDiff(getDiffCallback(items))
				runOnUiThread {
					diffResult.dispatchUpdatesTo(this)

					mutableList.clear()
					mutableList.addAll(items)

					callback.onSuccess(true)
				}
			}
		}
		
		return single.observeOn(Schedulers.io())
	}

	override fun setItem(newItem: I, position: Int): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {
				val valid = isUnique(newItem) && isValidPosition(position)
				if (valid) {
					mutableList.add(position, newItem)
					runOnUiThread {
						notifyListItemInserted(position)
					}
				}
				callback.onSuccess(false)
			}
		}

		return single.observeOn(Schedulers.io())
	}

	override fun prependItem(newItem: I): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {
				val valid = isUnique(newItem)
				if (valid) {

					fullList.add(0, newItem)

					if (currentConstraint.isEmpty() || filter.satisfy(newItem, currentConstraint)) {
						mutableList.add(0, newItem)

						runOnUiThread {
							notifyListItemInserted(0)
						}
					}
				}
				callback.onSuccess(valid)
			}
		}

		return single.observeOn(Schedulers.io())
	}

	override fun prependItems(newItems: List<I>): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {
				var result = newItems.isEmpty()

				if (!result) {
					val items =
						if (autoSort) newItems.sorted()
						else newItems

					// TODO maybe should check uniqueness
					fullList.addAll(0, items)

					if (currentConstraint.isEmpty()) {
						mutableList.addAll(0, items)

						runOnUiThread {
							notifyListItemRangeInserted(0, items.size)
						}
						result = true
					}
					else {
						val setisfactoryNewItems = LinkedList<I>().apply {
							for (candidate in items) {
								if (filter.satisfy(candidate, currentConstraint))
									add(candidate)
							}
						}
						if (setisfactoryNewItems.isNotEmpty()) {
							mutableList.addAll(0, setisfactoryNewItems)

							runOnUiThread {
								notifyListItemRangeInserted(0, setisfactoryNewItems.size)
							}
							result = true
						}
					}
				}
				callback.onSuccess(result)
			}
		}

		return single.observeOn(Schedulers.io())
	}

	override fun appendItem(newItem: I): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {
				val valid = isUnique(newItem)
				if (valid) {

					fullList.add(newItem)

					if (currentConstraint.isEmpty() || filter.satisfy(newItem, currentConstraint)) {
						mutableList.add(newItem)

						runOnUiThread {
							notifyListItemInserted(getLastItemPosition())
						}
					}
				}
				callback.onSuccess(valid)
			}
		}

		return single.observeOn(Schedulers.io())
	}

	override fun appendItems(newItems: List<I>): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {
				var result = newItems.isEmpty()

				if (!result) {

					val items =
						if (autoSort) newItems.sorted()
						else newItems

					// TODO maybe should check uniqueness
					fullList.addAll(items)

					val startPos = itemCount
					if (currentConstraint.isEmpty()) {
						mutableList.addAll(items)

						runOnUiThread {
							notifyListItemRangeInserted(startPos, getLastItemPosition())
						}
						result = true
					}
					else {
						val setisfactoryNewItems = LinkedList<I>().apply {
							for (candidate in items) {
								if (filter.satisfy(candidate, currentConstraint))
									add(candidate)
							}
						}
						if (setisfactoryNewItems.isNotEmpty()) {
							mutableList.addAll(setisfactoryNewItems)

							runOnUiThread {
								notifyListItemRangeInserted(startPos, getLastItemPosition())
							}
							result = true
						}
					}
					callback.onSuccess(result)
				}
			}
		}

		return single.observeOn(Schedulers.io())
	}

	override fun updateItem(updatedItem: I): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {
				val position = getFullListPosition(updatedItem)

				if (position != -1) fullList[position] = updatedItem

				callback.onSuccess(updateItemAt(getItemPosition(updatedItem), updatedItem))
			}
		}

		return single.observeOn(Schedulers.io())
	}

	override fun deleteItem(itemToDelete: I): Single<Boolean> {

		val single = Single.create<Boolean> { callback ->

			runAsync {
				val position = getFullListPosition(itemToDelete)

				if (position != -1) deleteItemAt(position)

				callback.onSuccess(deleteItemAt(getItemPosition(itemToDelete)))
			}
		}

		return single.observeOn(Schedulers.io())
	}

	private fun updateItemAt(position: Int, updatedItem: I): Boolean {

		if (isValidPosition(position)) {
			mutableList[position] = updatedItem
			runOnUiThread {
			}
			notifyListItemChanged(position)
			return true
		}

		return false
	}

	private fun deleteItemAt(position: Int): Boolean {

		if (isValidPosition(position)) {
			mutableList.removeAt(position)
			runOnUiThread {
			}
			notifyListItemRemoved(position)
			return true
		}

		return false
	}
}
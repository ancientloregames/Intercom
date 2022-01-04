package com.ancientlore.intercom.ui.boadcast.list

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.databinding.BroadcastListItemBinding
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.HeadedRecyclerDiffUtil
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter

class BroadcastListAdapter(context: Context,
                           items: MutableList<Chat> = mutableListOf())
	: MutableRecyclerAdapter<Chat, BroadcastListAdapter.ViewHolder, BroadcastListItemBinding>(
	context = context,
	items = items,
	autoSort = true) {

	interface Listener {
		fun onChatSelected(chat: Chat)
		fun onItemLongClick(chat: Chat)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Chat>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): BroadcastListItemBinding =
		BroadcastListItemBinding.inflate(layoutInflater, parent, false)

	override fun bindItemViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {

		val chat = getItem(position)!!

		holder.bind(chat)

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onChatSelected(chat)
			}
			override fun onItemLongClicked() {
				listener?.onItemLongClick(chat)
			}
		}
	}

	override fun createItemViewHolder(binding: BroadcastListItemBinding, viewType: Int) = ViewHolder(binding)

	override fun isTheSame(first: Chat, second: Chat) = first.id == second.id

	override fun isUnique(item: Chat) = getItems().none { it.id == item.id }

	override fun createFilter() = Filter()

	fun setListener(listener: Listener?) { this.listener = listener }

	class ViewHolder(binding: BroadcastListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Chat, BroadcastListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
			fun onItemLongClicked()
		}

		var listener: Listener? = null

		val iconField = ObservableField<Any>()
		val titleField = ObservableField("")

		@ColorInt
		private val iconColor: Int
		@Px
		private val iconTextSize: Int

		init {
			binding.setVariable(BR.viewModel, this)

			iconColor = ContextCompat.getColor(context, R.color.chatIconBackColor)
			iconTextSize = resources.getDimensionPixelSize(R.dimen.chatListIconTextSize)
		}

		override fun bind(data: Chat) {

			val name = data.localName ?: data.name

			titleField.set(name)

			iconField.set(when {
				data.iconUrl.isNotEmpty() -> data.iconUrl
				else -> ImageUtils.createAbbreviationDrawable(name, iconColor, iconTextSize)
			})
		}

		fun onClick() = listener?.onItemClicked()

		fun onLongClick(): Boolean {
			listener?.onItemLongClicked()
			return true
		}
	}

	class DiffCallback(private val oldItems: List<Chat>,
	                   private val newItems: List<Chat>)
		: HeadedRecyclerDiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].id == newItems[newPos].id

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]
	}

	inner class Filter: ListFilter() {
		override fun satisfy(item: Chat, candidate: String) = item.contains(candidate)
	}
}
package com.ancientlore.intercom.ui.chat.list

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.R
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.databinding.ChatListItemBinding
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter

class ChatListAdapter(context: Context, items: MutableList<Chat>)
	: MutableRecyclerAdapter<Chat, ChatListAdapter.ViewHolder, ChatListItemBinding>(context, items) {

	interface Listener {
		fun onChatSelected(chat: Chat)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Chat>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ChatListItemBinding =
		ChatListItemBinding.inflate(layoutInflater, parent, false)

	override fun bindItemViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {

		val chat = getItem(position)!!

		holder.bind(chat)

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onChatSelected(chat)
			}
		}
	}

	override fun createItemViewHolder(binding: ChatListItemBinding, viewType: Int) = ViewHolder(binding)

	override fun isTheSame(first: Chat, second: Chat) = first.id == second.id

	override fun isUnique(item: Chat) = getItems().none { it.id == item.id }

	override fun createFilter() = Filter()

	fun setListener(listener: Listener) { this.listener = listener }

	class ViewHolder(binding: ChatListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Chat, ChatListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}

		var listener: Listener? = null

		val icon = ObservableField<Any>(EmptyObject)
		val titleField = ObservableField<String>("")
		val messageField = ObservableField<String>("")
		val dateField = ObservableField<String>("")

		private val iconColor: Int
		private val iconTextSize: Int

		init {
			binding.setVariable(BR.chat, this)

			iconColor = ContextCompat.getColor(context, R.color.chatIconBackColor)
			iconTextSize = resources.getDimensionPixelSize(R.dimen.chatListIconTextSize)
		}

		override fun bind(data: Chat) {
			val name = data.localName ?: data.name
			titleField.set(name)
			messageField.set(data.lastMsgText)
			dateField.set(data.lastMsgDate)

			when {
				data.iconUrl.isNotEmpty() -> icon.set(data.iconUri)
				else -> icon.set(ImageUtils.createAbbreviationDrawable(name, iconColor, iconTextSize))
			}
		}

		fun onClick() = listener?.onItemClicked()
	}

	class DiffCallback(private val oldItems: List<Chat>,
	                   private val newItems: List<Chat>)
		: DiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].id == newItems[newPos].id

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]
	}

	inner class Filter: ListFilter() {
		override fun satisfy(item: Chat, candidate: String) = item.contains(candidate)
	}
}
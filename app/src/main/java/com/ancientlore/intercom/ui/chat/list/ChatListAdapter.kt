package com.ancientlore.intercom.ui.chat.list

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.databinding.ChatListItemBinding

class ChatListAdapter(context: Context, items: MutableList<Chat>)
	: MutableRecyclerAdapter<Chat, ChatListAdapter.ViewHolder, ChatListItemBinding>(context, items) {

	interface Listener {
		fun onChatSelected(chat: Chat)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Chat>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ChatListItemBinding =
		ChatListItemBinding.inflate(layoutInflater, parent, false)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		super.onBindViewHolder(holder, position)

		val chat = getItem(position)!!

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onChatSelected(chat)
			}
		}
	}

	override fun getViewHolder(binding: ChatListItemBinding, viewType: Int) = ViewHolder(binding)

	override fun isTheSame(first: Chat, second: Chat) = first.chatId == second.chatId

	override fun isUnique(item: Chat) = getItems().none { it.chatId == item.chatId }

	fun setListener(listener: Listener) { this.listener = listener }

	class ViewHolder(binding: ChatListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Chat, ChatListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}

		var listener: Listener? = null

		val titleField = ObservableField<String>("")
		val messageField = ObservableField<String>("")
		val dateField = ObservableField<String>("")

		init {
			binding.setVariable(BR.chat, this)
		}

		override fun bind(chat: Chat) {
			titleField.set(chat.name)
			messageField.set(chat.lastMsgText)
			dateField.set(chat.lastMsgDate)
		}

		fun onClick() = listener?.onItemClicked()
	}

	class DiffCallback(private val oldItems: List<Chat>,
	                   private val newItems: List<Chat>)
		: DiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].chatId == newItems[newPos].chatId

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]
	}
}
package com.ancientlore.intercom.ui.chat.list

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.ui.BasicRecyclerAdapter
import com.ancientlore.intercom.ui.MutableRecyclerAdapter
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.databinding.ChatListItemBinding

class ChatListAdapter(context: Context, items: MutableList<Chat>)
	: MutableRecyclerAdapter<Chat, ChatListAdapter.ViewHolder, ChatListItemBinding>(context, items) {

	interface Listener {
		fun onChatSelected(contact: Chat)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Chat>): DiffUtil.Callback {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun deleteItem(id: Long): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun createItemViewDataBinding(parent: ViewGroup): ChatListItemBinding =
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

	override fun getViewHolder(binding: ChatListItemBinding) = ViewHolder(binding)

	override fun isTheSame(first: Chat, second: Chat) = first.id == second.id

	override fun isUnique(item: Chat) = getItems().none { it.id == item.id }

	fun setListener(listener: Listener) { this.listener = listener }

	class ViewHolder(binding: ChatListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Chat, ChatListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}

		var listener: Listener? = null

		val titleField = ObservableField<String>("")

		init {
			binding.setVariable(BR.chat, this)
		}

		override fun bind(chat: Chat) {
			titleField.set(chat.title ?: chat.participants[0])
		}

		fun onClick() = listener?.onItemClicked()
	}
}
package com.ancientlore.intercom.ui.chat.list

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.ui.BasicRecyclerAdapter
import com.ancientlore.intercom.ui.MutableRecyclerAdapter
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.databinding.ChatListItemBinding

class ChatListAdapter(context: Context, items: MutableList<Chat>)
	: MutableRecyclerAdapter<Chat, ChatListAdapter.ViewHolder, ChatListItemBinding>(context, items) {

	override fun getDiffCallback(newItems: List<Chat>): DiffUtil.Callback {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun deleteItem(id: Long): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun createItemViewDataBinding(parent: ViewGroup): ChatListItemBinding {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getViewHolder(binding: ChatListItemBinding): ViewHolder {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isTheSame(first: Chat, second: Chat): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isUnique(item: Chat): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	class ViewHolder(binding: ChatListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Chat, ChatListItemBinding>(binding) {

		override fun bind(data: Chat) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}
	}
}
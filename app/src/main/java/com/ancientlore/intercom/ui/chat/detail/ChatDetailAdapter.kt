package com.ancientlore.intercom.ui.chat.detail

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.databinding.ChatListItemBinding
import com.ancientlore.intercom.ui.BasicRecyclerAdapter
import com.ancientlore.intercom.ui.MutableRecyclerAdapter

class ChatDetailAdapter(context: Context, items: MutableList<Message>)
	: MutableRecyclerAdapter<Message, ChatDetailAdapter.ViewHolder, ChatListItemBinding>(context, items) {

	override fun getDiffCallback(newItems: List<Message>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup): ChatListItemBinding =
		ChatListItemBinding.inflate(layoutInflater, parent, false)

	override fun getViewHolder(binding: ChatListItemBinding) = ViewHolder(binding)

	override fun isTheSame(first: Message, second: Message) = first.timestamp == second.timestamp

	override fun isUnique(item: Message) = getItems().none { it.timestamp == item.timestamp }

	class ViewHolder(binding: ChatListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Message, ChatListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}

		var listener: Listener? = null

		val textField = ObservableField<String>("")
		val timestampField = ObservableField<String>("")

		init {
			binding.setVariable(BR.message, this)
		}

		override fun bind(message: Message) {
			textField.set(message.text)
			timestampField.set(message.timestamp.toString())
		}

		fun onClick() = listener?.onItemClicked()
	}

	class DiffCallback(private val oldItems: List<Message>,
	                   private val newItems: List<Message>)
		: DiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].timestamp == newItems[newPos].timestamp

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]
	}
}
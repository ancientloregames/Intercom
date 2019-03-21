package com.ancientlore.intercom.ui.chat.flow

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.databinding.ChatFlowItemOtherBinding
import com.ancientlore.intercom.databinding.ChatFlowItemUserBinding
import com.ancientlore.intercom.ui.BasicRecyclerAdapter
import com.ancientlore.intercom.ui.MutableRecyclerAdapter

class ChatFlowAdapter(private val userId: String,
                      context: Context, items: MutableList<Message>)
	: MutableRecyclerAdapter<Message, ChatFlowAdapter.ViewHolder, ViewDataBinding>(context, items) {

	private companion object {
		private const val VIEW_TYPE_USER = 0
		private const val VIEW_TYPE_OTHER = 1
	}

	override fun getDiffCallback(newItems: List<Message>) = DiffCallback(getItems(), newItems)

	override fun getItemViewType(position: Int): Int {
		val message = getItem(position)!!
		return when (message.senderId) {
			userId -> VIEW_TYPE_USER
			else -> VIEW_TYPE_OTHER
		}
	}

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
		return when (viewType) {
			VIEW_TYPE_USER -> ChatFlowItemUserBinding.inflate(layoutInflater, parent, false)
			else -> ChatFlowItemOtherBinding.inflate(layoutInflater, parent, false)
		}
	}

	override fun getViewHolder(binding: ViewDataBinding, viewType: Int) = ViewHolder(binding)

	override fun isTheSame(first: Message, second: Message) = first.timestamp == second.timestamp

	override fun isUnique(item: Message) = getItems().none { it.timestamp == item.timestamp }

	class ViewHolder(binding: ViewDataBinding)
		: BasicRecyclerAdapter.ViewHolder<Message, ViewDataBinding>(binding) {

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
			timestampField.set(message.formatedTime)
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
package com.ancientlore.intercom.ui.chat.flow

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.databinding.ChatFlowFileItemOtherBinding
import com.ancientlore.intercom.databinding.ChatFlowFileItemUserBinding
import com.ancientlore.intercom.databinding.ChatFlowItemOtherBinding
import com.ancientlore.intercom.databinding.ChatFlowItemUserBinding
import com.ancientlore.intercom.utils.extensions.isNotEmpty
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import java.lang.RuntimeException

class ChatFlowAdapter(private val userId: String,
                      context: Context, items: MutableList<Message>)
	: MutableRecyclerAdapter<Message, ChatFlowAdapter.ViewHolder, ViewDataBinding>(context, items) {

	private companion object {
		private const val VIEW_TYPE_USER = 0
		private const val VIEW_TYPE_OTHER = 1
		private const val VIEW_TYPE_FILE_USER = 2
		private const val VIEW_TYPE_FILE_OTHER = 3
	}

	override fun getDiffCallback(newItems: List<Message>) = DiffCallback(getItems(), newItems)

	override fun getItemViewType(position: Int): Int {
		val message = getItem(position)!!
		return when (message.senderId) {
			userId -> when (message.type) {
				Message.TYPE_FILE -> VIEW_TYPE_FILE_USER
				else -> VIEW_TYPE_USER
			}
			else ->  when (message.type) {
				Message.TYPE_FILE -> VIEW_TYPE_FILE_OTHER
				else -> VIEW_TYPE_OTHER
			}
		}
	}

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
		return when (viewType) {
			VIEW_TYPE_USER -> ChatFlowItemUserBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_OTHER -> ChatFlowItemOtherBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_FILE_USER -> ChatFlowFileItemUserBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_FILE_OTHER -> ChatFlowFileItemOtherBinding.inflate(layoutInflater, parent, false)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun getViewHolder(binding: ViewDataBinding, viewType: Int): ViewHolder {
		return when (viewType) {
			VIEW_TYPE_USER, VIEW_TYPE_OTHER -> ItemViewHolder(binding)
			VIEW_TYPE_FILE_USER, VIEW_TYPE_FILE_OTHER -> FileItemViewHolder(binding)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
		if (payloads.isNotEmpty())
			holder.bind(payloads[0] as Bundle)
		else super.onBindViewHolder(holder, position)
	}

	override fun isTheSame(first: Message, second: Message) = first.timestamp == second.timestamp

	override fun isUnique(item: Message) = getItems().none { it.timestamp == item.timestamp }

	class FileItemViewHolder(binding: ViewDataBinding)
		: ViewHolder(binding) {

		val titleField = ObservableField<String>("")
		val subtitleField = ObservableField<String>("")

		init {
			binding.setVariable(BR.message, this)
		}

		override fun bind(data: Message) {
			super.bind(data)
			titleField.set(data.text)
			subtitleField.set(data.info)
		}
	}

	class ItemViewHolder(binding: ViewDataBinding)
		: ViewHolder(binding) {

		val textField = ObservableField<String>("")
		val imageUri = ObservableField<Uri>(Uri.EMPTY)
		val imageVisibility = ObservableBoolean()

		init {
			binding.setVariable(BR.message, this)
		}

		override fun bind(data: Message) {
			super.bind(data)
			textField.set(data.text)
			imageVisibility.set(data.attachUri.isNotEmpty())
			imageUri.set(data.attachUri)
		}

		override fun bind(payload: Bundle) {
			super.bind(payload)

			val newText = payload.getString(DiffCallback.KEY_TEXT)
			if (newText != null)
				textField.set(newText)

			val newImageUrl = payload.getString(DiffCallback.KEY_URL)
			if (newImageUrl != null)
				imageUri.set(Uri.parse(newImageUrl))
		}
	}

	abstract class ViewHolder(binding: ViewDataBinding)
		: BasicRecyclerAdapter.ViewHolder<Message, ViewDataBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}

		var listener: Listener? = null

		val timestampField = ObservableField<String>("")
		val statusIconRes = ObservableField<Int>()

		@CallSuper
		override fun bind(data: Message) {
			timestampField.set(data.formatedTime)

			statusIconRes.set(getStatusResId(data.status))
		}

		@CallSuper
		open fun bind(payload: Bundle) {
			val newStatus = payload.getInt(DiffCallback.KEY_STATUS, -1)
			if (newStatus != -1)
				statusIconRes.set(getStatusResId(newStatus))
		}

		@DrawableRes
		private fun getStatusResId(@Message.Status status: Int) : Int {
			return when (status) {
				Message.STATUS_SENT -> R.drawable.ic_send_check
				Message.STATUS_RECEIVED -> R.drawable.ic_send_check_all
				else -> R.drawable.ic_send_wait
			}
		}

		fun onClick() = listener?.onItemClicked()
	}

	class DiffCallback(private val oldItems: List<Message>,
	                   private val newItems: List<Message>)
		: DiffUtil.Callback() {

		companion object {
			const val KEY_URL = "url"
			const val KEY_TEXT = "text"
			const val KEY_STATUS = "status"
		}

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].timestamp == newItems[newPos].timestamp

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]

		override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
			val oldMessage = oldItems[oldItemPosition]
			val newMessage = newItems[newItemPosition]

			val bundle = Bundle().apply {
				if (newMessage.attachUrl != oldMessage.attachUrl)
					putString(KEY_URL, newMessage.attachUrl)
				if (newMessage.text != oldMessage.text)
					putString(KEY_TEXT, newMessage.text)
				if (newMessage.status != oldMessage.status)
					putInt(KEY_STATUS, newMessage.status)
			}

			return if (bundle.isNotEmpty()) bundle else null
		}
	}
}
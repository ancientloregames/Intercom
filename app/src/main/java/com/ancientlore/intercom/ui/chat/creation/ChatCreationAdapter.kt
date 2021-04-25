package com.ancientlore.intercom.ui.chat.creation

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ChatCreationFooterBinding
import com.ancientlore.intercom.databinding.ChatCreationHeaderBinding
import com.ancientlore.intercom.databinding.ChatCreationItemBinding
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import java.lang.RuntimeException

class ChatCreationAdapter(context: Context, items: MutableList<Contact>)
	: MutableRecyclerAdapter<Contact, ChatCreationAdapter.ViewHolder<ViewDataBinding>, ViewDataBinding>(
			context, items, withHeader = true) {

	companion object {
		private const val VIEW_TYPE_HEADER = Int.MIN_VALUE
		private const val VIEW_TYPE_FOOTER = Int.MAX_VALUE
		private const val VIEW_TYPE_ITEM = 0
	}

	interface Listener {
		fun onContactSelected(contact: Contact)
		fun onCreateGroup()
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Contact>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
		return when (viewType) {
			VIEW_TYPE_ITEM -> ChatCreationItemBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_HEADER -> ChatCreationHeaderBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_FOOTER -> ChatCreationFooterBinding.inflate(layoutInflater, parent, false)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun createHeaderViewHolder(binding: ViewDataBinding): ViewHolder<ViewDataBinding> {
		return HeaderViewHolder(binding as ChatCreationHeaderBinding) as ViewHolder<ViewDataBinding>
	}

	override fun createFooterViewHolder(binding: ViewDataBinding): ViewHolder<ViewDataBinding> {
		return FooterViewHolder(binding as ChatCreationFooterBinding) as ViewHolder<ViewDataBinding>
	}

	override fun createItemViewHolder(binding: ViewDataBinding, viewType: Int): ViewHolder<ViewDataBinding> {
		return ItemViewHolder(binding as ChatCreationItemBinding) as ViewHolder<ViewDataBinding>
	}

	override fun bindHeaderViewHolder(holder: ViewHolder<ViewDataBinding>, payloads: MutableList<Any>) {
		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onCreateGroup()
			}
		}
	}

	override fun bindFooterViewHolder(holder: ViewHolder<ViewDataBinding>, payloads: MutableList<Any>) {}

	override fun bindItemViewHolder(holder: ViewHolder<ViewDataBinding>, position: Int, payloads: MutableList<Any>) {

		val contact = getItem(position)!!

		holder.bind(contact)

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onContactSelected(contact)
			}
		}
	}

	override fun isTheSame(first: Contact, second: Contact) = first.phone == second.phone

	override fun isUnique(item: Contact) = getItems().none { it.phone == item.phone }

	fun setListener(listener: Listener) { this.listener = listener }

	abstract class ViewHolder<B: ViewDataBinding>(binding: B)
		: BasicRecyclerAdapter.ViewHolder<Contact, B>(binding) {

		interface Listener {
			fun onItemClicked()
		}
		var listener: Listener? = null

		fun onClick() = listener?.onItemClicked()
	}

	class HeaderViewHolder(binding: ChatCreationHeaderBinding)
		: ViewHolder<ChatCreationHeaderBinding>(binding) {

		init {
			binding.setVariable(BR.ui, this)
		}

		override fun bind(data: Contact) {}
	}

	class FooterViewHolder(binding: ChatCreationFooterBinding)
		: ViewHolder<ChatCreationFooterBinding>(binding) {

		init {
			binding.setVariable(BR.ui, this)
		}

		override fun bind(data: Contact) {}
	}

	class ItemViewHolder(binding: ChatCreationItemBinding)
		: ViewHolder<ChatCreationItemBinding>(binding) {

		val nameField = ObservableField("")
		val subtitleField = ObservableField("")
		val photoUri = ObservableField(Uri.EMPTY)

		init {
			binding.setVariable(BR.ui, this)
		}

		override fun bind(data: Contact) {
			nameField.set(data.name)
			subtitleField.set(data.phone)
		}
	}

	class DiffCallback(private val oldItems: List<Contact>,
	                   private val newItems: List<Contact>)
		: DiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].phone == newItems[newPos].phone

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]
	}

	override fun createFilter() = Filter()

	inner class Filter: ListFilter() {
		override fun satisfy(item: Contact, candidate: String) = item.contains(candidate)
	}
}
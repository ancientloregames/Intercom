package com.ancientlore.intercom.ui.chat.creation.group

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ChatCreationGroupItemBinding
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import java.lang.RuntimeException

class ChatCreationGroupAdapter(context: Context,
                               items: MutableList<Contact> = mutableListOf())
	: MutableRecyclerAdapter<Contact, ChatCreationGroupAdapter.ViewHolder<ViewDataBinding>, ViewDataBinding>(
	context, items) {

	interface Listener {
		fun onContactSelected(contact: Contact)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Contact>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
		return when (viewType) {
			VIEW_TYPE_ITEM -> ChatCreationGroupItemBinding.inflate(layoutInflater, parent, false)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun createItemViewHolder(binding: ViewDataBinding, viewType: Int): ViewHolder<ViewDataBinding> {
		return ItemViewHolder(binding as ChatCreationGroupItemBinding) as ViewHolder<ViewDataBinding>
	}

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

		open fun onClick() = listener?.onItemClicked()
	}

	class ItemViewHolder(binding: ChatCreationGroupItemBinding)
		: ViewHolder<ChatCreationGroupItemBinding>(binding) {

		val nameField = ObservableField("")
		val subtitleField = ObservableField("")
		val photoUri = ObservableField(Uri.EMPTY)

		val checkboxCheckedField = ObservableBoolean(false)

		init {
			binding.setVariable(BR.ui, this)
		}

		override fun bind(data: Contact) {
			nameField.set(data.name)
			subtitleField.set(data.phone)
		}

		override fun onClick() {
			checkboxCheckedField.set(checkboxCheckedField.get().not())

			super.onClick()
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
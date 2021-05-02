package com.ancientlore.intercom.ui.contact.list

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.databinding.ContactListItemBinding
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter

class ContactListAdapter(context: Context,
                         items: MutableList<Contact> = mutableListOf())
	: MutableRecyclerAdapter<Contact, ContactListAdapter.ViewHolder, ContactListItemBinding>(context, items) {

	interface Listener {
		fun onContactSelected(contact: Contact)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Contact>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ContactListItemBinding =
		ContactListItemBinding.inflate(layoutInflater, parent, false)

	override fun bindItemViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {

		val contact = getItem(position)!!

		holder.bind(contact)

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onContactSelected(contact)
			}
		}
	}

	override fun createItemViewHolder(binding: ContactListItemBinding, viewType: Int) = ViewHolder(binding)

	override fun isTheSame(first: Contact, second: Contact) = first.phone == second.phone

	override fun isUnique(item: Contact) = getItems().none { it.phone == item.phone }

	fun setListener(listener: Listener) { this.listener = listener }

	class ViewHolder(binding: ContactListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Contact, ContactListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}
		var listener: Listener? = null

		val nameField = ObservableField("")
		val subtitleField = ObservableField("")
		val photoUri = ObservableField(Uri.EMPTY)

		init {
			binding.setVariable(BR.contact, this)
		}

		override fun bind(data: Contact) {
			nameField.set(data.name)
			subtitleField.set(data.phone)
		}

		fun onClick() = listener?.onItemClicked()
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
		override fun satisfy(item: Contact, candidate: String) =
			item.name.contains(candidate, true) || item.phone.contains(candidate, true)
	}
}
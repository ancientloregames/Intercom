package com.ancientlore.intercom.ui.contact.list

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ContactListItemBinding
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter

class ContactListAdapter(context: Context, items: MutableList<Contact>)
	: MutableRecyclerAdapter<Contact, ContactListAdapter.ViewHolder, ContactListItemBinding>(context, items) {

	interface Listener {
		fun onContactSelected(contact: Contact)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<Contact>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ContactListItemBinding =
		ContactListItemBinding.inflate(layoutInflater, parent, false)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		super.onBindViewHolder(holder, position)

		val contact = getItem(position)!!

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onContactSelected(contact)
			}
		}
	}

	override fun getViewHolder(binding: ContactListItemBinding, viewType: Int) = ViewHolder(binding)

	override fun isTheSame(first: Contact, second: Contact) = first.phone == second.phone

	override fun isUnique(item: Contact) = getItems().none { it.phone == item.phone }

	fun setListener(listener: Listener) { this.listener = listener }

	class ViewHolder(binding: ContactListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Contact, ContactListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}
		var listener: Listener? = null

		val nameField = ObservableField<String>("")
		val subtitleField = ObservableField<String>("")
		val photoUri = ObservableField<Uri>(Uri.EMPTY)

		init {
			binding.setVariable(BR.contact, this)
		}

		override fun bind(data: Contact) {
			nameField.set(data.name)
			subtitleField.set(data.lastSeenDate)
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
}
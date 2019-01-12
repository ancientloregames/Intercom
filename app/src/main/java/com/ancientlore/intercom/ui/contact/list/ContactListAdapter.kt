package com.ancientlore.intercom.ui.contact.list

import android.content.Context
import android.view.ViewGroup
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.ui.BasicRecyclerAdapter
import com.ancientlore.intercom.data.Contact
import com.ancientlore.intercom.databinding.ContactListItemBinding

class ContactListAdapter(context: Context, items: List<Contact>)
	: BasicRecyclerAdapter<Contact, ContactListAdapter.ViewHolder, ContactListItemBinding>(context, items) {

	override fun createItemViewDataBinding(parent: ViewGroup): ContactListItemBinding =
		ContactListItemBinding.inflate(layoutInflater, parent, false)

	override fun getViewHolder(binding: ContactListItemBinding) = ViewHolder(binding)

	override fun isTheSame(first: Contact, second: Contact) = first.id == second.id

	override fun isUnique(item: Contact) = getItems().none { it.id == item.id }

	class ViewHolder(binding: ContactListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Contact, ContactListItemBinding>(binding) {

		interface Listener {
			fun onItemClicked()
		}
		var listener: Listener? = null

		init {
			binding.setVariable(BR.contact, this)
		}

		override fun bind(data: Contact) {
		}

		fun onClick() = listener?.onItemClicked()
	}
}
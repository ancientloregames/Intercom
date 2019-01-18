package com.ancientlore.intercom.ui.contact.list

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.databinding.ObservableField
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.ui.BasicRecyclerAdapter
import com.ancientlore.intercom.data.model.Contact
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

		val nameField = ObservableField<String>("")
		val phoneField = ObservableField<String>("")
		val photoUri = ObservableField<Uri>(Uri.EMPTY)

		init {
			binding.setVariable(BR.contact, this)
		}

		override fun bind(data: Contact) {
			nameField.set(data.name)
			phoneField.set(data.phone)
			photoUri.set(data.photoUri)
		}

		fun onClick() = listener?.onItemClicked()
	}
}
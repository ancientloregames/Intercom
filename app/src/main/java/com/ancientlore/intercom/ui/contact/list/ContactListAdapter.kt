package com.ancientlore.intercom.ui.contact.list

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.ui.BasicRecyclerAdapter
import com.ancientlore.intercom.ui.MutableRecyclerAdapter
import com.ancientlore.intercom.data.Contact
import com.ancientlore.intercom.databinding.ContactListItemBinding

class ContactListAdapter(context: Context, items: MutableList<Contact>)
	: MutableRecyclerAdapter<Contact, ContactListAdapter.ViewHolder, ContactListItemBinding>(context, items) {
	override fun getDiffCallback(newItems: List<Contact>): DiffUtil.Callback {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun deleteItem(id: Long): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun createItemViewDataBinding(parent: ViewGroup): ContactListItemBinding {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getViewHolder(binding: ContactListItemBinding): ViewHolder {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getItem(id: Long): Contact? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getItemPosition(id: Long): Int? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isTheSame(first: Contact, second: Contact): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isUnique(item: Contact): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	class ViewHolder(binding: ContactListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<Contact, ContactListItemBinding>(binding) {

		override fun bind(data: Contact) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}
	}
}
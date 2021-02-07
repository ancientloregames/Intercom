package com.ancientlore.intercom.ui.contact.list

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.databinding.ContactListItemBinding
import com.ancientlore.intercom.manager.DeviceContactsManager
import com.ancientlore.intercom.widget.recycler.FilterableRecyclerAdapter

class ContactListAdapter(context: Context, items: MutableList<DeviceContactsManager.Item>)
	: FilterableRecyclerAdapter<DeviceContactsManager.Item, ContactListAdapter.ViewHolder, ContactListItemBinding>(context, items) {

	interface Listener {
		fun onContactSelected(contact: DeviceContactsManager.Item)
	}

	private var listener: Listener? = null

	override fun getDiffCallback(newItems: List<DeviceContactsManager.Item>) = DiffCallback(getItems(), newItems)

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

	override fun isTheSame(first: DeviceContactsManager.Item, second: DeviceContactsManager.Item) = first.id == second.id

	override fun isUnique(item: DeviceContactsManager.Item) = getItems().none { it.id == item.id }

	fun setListener(listener: Listener) { this.listener = listener }

	class ViewHolder(binding: ContactListItemBinding)
		: BasicRecyclerAdapter.ViewHolder<DeviceContactsManager.Item, ContactListItemBinding>(binding) {

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

		override fun bind(data: DeviceContactsManager.Item) {
			nameField.set(data.name)
			subtitleField.set(data.id)
		}

		fun onClick() = listener?.onItemClicked()
	}

	class DiffCallback(private val oldItems: List<DeviceContactsManager.Item>,
	                   private val newItems: List<DeviceContactsManager.Item>)
		: DiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].id == newItems[newPos].id

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]
	}

	override fun createFilter() = Filter()

	inner class Filter: ListFilter() {
		override fun satisfy(item: DeviceContactsManager.Item, candidate: String) =
			item.name.contains(candidate, true) || item.mainNumber.contains(candidate, true)
	}
}
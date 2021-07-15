package com.ancientlore.intercom.ui.chat.creation.group

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ChatCreationGroupItemBinding
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.extensions.isNotEmpty
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.HeadedRecyclerDiffUtil
import java.lang.RuntimeException

class ChatCreationGroupAdapter(context: Context,
                               items: MutableList<Contact> = mutableListOf())
	: MutableRecyclerAdapter<Contact, ChatCreationGroupAdapter.ViewHolder<ViewDataBinding>, ViewDataBinding>(
	context, items) {

	companion object {
		const val PAYLOAD_KEY_CHECK = "check"
	}

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

		if (payloads.isNotEmpty())
			holder.bind(payloads[0] as Bundle)
		else
			holder.bind(contact)

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onContactSelected(contact)
			}
		}
	}

	override fun isTheSame(first: Contact, second: Contact) = first.phone == second.phone

	override fun isUnique(item: Contact) = getItems().none { it.phone == item.phone }

	fun findItemIndex(contactId: String) = getItems().indexOfFirst { it.getIdentity() == contactId }

	fun switchCheckBoxItem(item: Contact) {

		findItemIndex(item.getIdentity())
			.takeIf { it != -1 }
			?.let {
				item.checked = item.checked.not()

				val bundle = Bundle().apply {
					putBoolean(PAYLOAD_KEY_CHECK, item.checked)
				}

				notifyListItemChanged(it, bundle)
			}
	}

	fun setListener(listener: Listener) { this.listener = listener }

	abstract class ViewHolder<B: ViewDataBinding>(binding: B)
		: BasicRecyclerAdapter.ViewHolder<Contact, B>(binding) {

		interface Listener {
			fun onItemClicked()
		}
		var listener: Listener? = null

		abstract fun bind(payload: Bundle)

		open fun onClick() = listener?.onItemClicked()
	}

	class ItemViewHolder(binding: ChatCreationGroupItemBinding)
		: ViewHolder<ChatCreationGroupItemBinding>(binding) {

		val nameField = ObservableField("")
		val subtitleField = ObservableField("")
		val iconField = ObservableField<Any>()

		val checkboxCheckedField = ObservableBoolean(false)

		@ColorInt
		private val iconAbbrColor: Int
		@Px
		private val iconAbbrTextSize: Int

		init {
			binding.setVariable(BR.ui, this)

			iconAbbrColor = ContextCompat.getColor(context, R.color.chatIconBackColor)
			iconAbbrTextSize = resources.getDimensionPixelSize(R.dimen.chatListIconTextSize)
		}

		override fun bind(data: Contact) {
			nameField.set(data.name)
			subtitleField.set(data.phone)

			iconField.set(when {
				data.iconUrl.isNotEmpty() -> data.iconUrl
				else -> ImageUtils.createAbbreviationDrawable(data.name, iconAbbrColor, iconAbbrTextSize)
			})
		}

		override fun bind(payload: Bundle) {

			checkboxCheckedField.set(payload.getBoolean(PAYLOAD_KEY_CHECK, false))
		}

		override fun onClick() {
			checkboxCheckedField.set(checkboxCheckedField.get().not())

			super.onClick()
		}
	}

	class DiffCallback(private val oldItems: List<Contact>,
	                   private val newItems: List<Contact>)
		: HeadedRecyclerDiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].phone == newItems[newPos].phone

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]

		override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
			val oldContact = oldItems[oldItemPosition]
			val newContact = newItems[newItemPosition]

			val bundle = Bundle().apply {
				if (newContact.checked != oldContact.checked)
					putBoolean(PAYLOAD_KEY_CHECK, newContact.checked)
			}

			return if (bundle.isNotEmpty()) bundle else null
		}
	}

	override fun createFilter() = Filter()

	inner class Filter: ListFilter() {
		override fun satisfy(item: Contact, candidate: String) = item.contains(candidate)
	}
}
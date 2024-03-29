package com.ancientlore.intercom.ui.chat.creation.description

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ChatCreationDescItemBinding
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.HeadedRecyclerDiffUtil
import java.lang.RuntimeException

class ChatCreationDescAdapter(context: Context,
                              items: MutableList<Contact> = mutableListOf())
	: MutableRecyclerAdapter<Contact, ChatCreationDescAdapter.ViewHolder<ViewDataBinding>, ViewDataBinding>(
	context, items) {

	override fun getDiffCallback(newItems: List<Contact>) = DiffCallback(getItems(), newItems)

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
		return when (viewType) {
			VIEW_TYPE_ITEM -> ChatCreationDescItemBinding.inflate(layoutInflater, parent, false)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun createItemViewHolder(binding: ViewDataBinding, viewType: Int): ViewHolder<ViewDataBinding> {
		return ItemViewHolder(binding as ChatCreationDescItemBinding) as ViewHolder<ViewDataBinding>
	}

	override fun bindItemViewHolder(holder: ViewHolder<ViewDataBinding>, position: Int, payloads: MutableList<Any>) {

		val contact = getItem(position)!!

		holder.bind(contact)
	}

	override fun isTheSame(first: Contact, second: Contact) = first.phone == second.phone

	override fun isUnique(item: Contact) = getItems().none { it.phone == item.phone }

	override fun createFilter() = Filter()

	abstract class ViewHolder<B: ViewDataBinding>(binding: B)
		: BasicRecyclerAdapter.ViewHolder<Contact, B>(binding) {

		interface Listener {
			fun onItemClicked()
		}
		var listener: Listener? = null

		open fun onClick() = listener?.onItemClicked()
	}

	class ItemViewHolder(binding: ChatCreationDescItemBinding)
		: ViewHolder<ChatCreationDescItemBinding>(binding) {

		val nameField = ObservableField("")
		val subtitleField = ObservableField("")
		val iconField = ObservableField<Any>()

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
				data.name.isNotEmpty() -> ImageUtils.createAbbreviationDrawable(data.name, iconAbbrColor, iconAbbrTextSize)
				else -> Uri.EMPTY
			})
		}
	}

	class DiffCallback(private val oldItems: List<Contact>,
	                   private val newItems: List<Contact>)
		: HeadedRecyclerDiffUtil.Callback() {

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].phone == newItems[newPos].phone

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]
	}

	inner class Filter: ListFilter() {
		override fun satisfy(item: Contact, candidate: String) = item.contains(candidate)
	}
}
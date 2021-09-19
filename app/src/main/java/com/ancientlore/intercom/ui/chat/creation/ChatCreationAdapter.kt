package com.ancientlore.intercom.ui.chat.creation

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.databinding.ChatCreationFooterBinding
import com.ancientlore.intercom.databinding.ChatCreationHeaderBinding
import com.ancientlore.intercom.databinding.ChatCreationItemBinding
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.HeadedRecyclerDiffUtil
import java.lang.RuntimeException

class ChatCreationAdapter(context: Context,
                          items: MutableList<Contact> = mutableListOf())
	: MutableRecyclerAdapter<Contact, ChatCreationAdapter.ViewHolder<ViewDataBinding>, ViewDataBinding>(
			context, items, withHeader = true) {

	interface Listener {
		fun onContactSelected(contact: Contact)
		fun onCreateGroup()
		fun onAddContact()
		fun onCreateBroadcast()
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
		holder as HeaderViewHolder

		holder.listener = object : ViewHolder.Listener {
			override fun onItemClicked() {
				listener?.onCreateGroup()
			}
		}
		holder.addContactListener = object : HeaderViewHolder.AddContactListener {
			override fun onClicked() {
				listener?.onAddContact()
			}
		}
		holder.createBroadcastListener = object : HeaderViewHolder.CreateBroadcastListener {
			override fun onClicked() {
				listener?.onCreateBroadcast()
			}
		}
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

	fun setListener(listener: Listener?) { this.listener = listener }

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

		interface AddContactListener {
			fun onClicked()
		}
		var addContactListener: AddContactListener? = null

		interface CreateBroadcastListener {
			fun onClicked()
		}
		var createBroadcastListener: CreateBroadcastListener? = null

		init {
			binding.setVariable(BR.ui, this)
		}

		override fun bind(data: Contact) {}

		fun onAddContactClick() = addContactListener?.onClicked()

		fun createBroadcastClick() = createBroadcastListener?.onClicked()
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
				else -> ImageUtils.createAbbreviationDrawable(data.name, iconAbbrColor, iconAbbrTextSize)
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

	override fun createFilter() = Filter()

	inner class Filter: ListFilter() {
		override fun satisfy(item: Contact, candidate: String) = item.contains(candidate)
	}
}
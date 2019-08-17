package com.ancientlore.intercom.ui.chat.flow

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import androidx.annotation.DrawableRes
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.databinding.ChatFlowFileItemOtherBinding
import com.ancientlore.intercom.databinding.ChatFlowFileItemUserBinding
import com.ancientlore.intercom.databinding.ChatFlowItemOtherBinding
import com.ancientlore.intercom.databinding.ChatFlowItemUserBinding
import com.ancientlore.intercom.utils.extensions.isNotEmpty
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.FilterableRecyclerAdapter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.lang.RuntimeException

class ChatFlowAdapter(private val userId: String,
                      context: Context, items: MutableList<Message>)
	: FilterableRecyclerAdapter<Message, ChatFlowAdapter.ViewHolder, ViewDataBinding>(context, items) {

	private companion object {
		private const val VIEW_TYPE_USER = 0
		private const val VIEW_TYPE_OTHER = 1
		private const val VIEW_TYPE_FILE_USER = 2
		private const val VIEW_TYPE_FILE_OTHER = 3
	}

	private val openFile = PublishSubject.create<Uri>()

	private val openImage = PublishSubject.create<Uri>()

	override fun getDiffCallback(newItems: List<Message>) = DiffCallback(getItems(), newItems)

	override fun getItemViewType(position: Int): Int {
		val message = getItem(position)!!
		return when (message.senderId) {
			userId -> when (message.type) {
				Message.TYPE_FILE -> VIEW_TYPE_FILE_USER
				else -> VIEW_TYPE_USER
			}
			else ->  when (message.type) {
				Message.TYPE_FILE -> VIEW_TYPE_FILE_OTHER
				else -> VIEW_TYPE_OTHER
			}
		}
	}

	override fun createItemViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
		return when (viewType) {
			VIEW_TYPE_USER -> ChatFlowItemUserBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_OTHER -> ChatFlowItemOtherBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_FILE_USER -> ChatFlowFileItemUserBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_FILE_OTHER -> ChatFlowFileItemOtherBinding.inflate(layoutInflater, parent, false)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun getViewHolder(binding: ViewDataBinding, viewType: Int): ViewHolder {
		return when (viewType) {
			VIEW_TYPE_USER, VIEW_TYPE_OTHER -> ItemViewHolder(binding)
			VIEW_TYPE_FILE_USER, VIEW_TYPE_FILE_OTHER -> FileItemViewHolder(binding)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
		val item = getItem(position)!!
		when (holder) {
			is ItemViewHolder -> holder.listener = object : ItemViewHolder.Listener {
				override fun onImageClick(uri: Uri) {
					openImage.onNext(uri)
				}
			}
			is FileItemViewHolder -> holder.listener = object : FileItemViewHolder.Listener {
				override fun onItemClick() {
					openFile.onNext(item.attachUri)
				}
			}
		}

		if (payloads.isNotEmpty())
			holder.bind(payloads[0] as Bundle)
		else super.onBindViewHolder(holder, position)
	}

	override fun isTheSame(first: Message, second: Message) = first.timestamp == second.timestamp

	override fun isUnique(item: Message) = getItems().none { it.timestamp == item.timestamp }

	override fun createFilter() = Filter()

	@UiThread
	fun setFileUploadProgress(messageId: String, progress: Int) : Boolean {
		val index = findItemIndex(messageId)
		getItem(index)
			?.takeIf { it.progress != progress }
			?.let { item ->
			item.progress = progress
			notifyItemChanged(index, Bundle().apply {
				putInt(DiffCallback.KEY_PROGRESS, progress)
			})
		}

		return index != -1
	}

	fun findItemIndex(messageId: String) = getItems().indexOfFirst { it.id == messageId }

	fun findItem(messageId: String) = getItems().find { it.id == messageId }

	class FileItemViewHolder(binding: ViewDataBinding)
		: ViewHolder(binding) {

		interface Listener {
			fun onItemClick()
		}

		var listener: Listener? = null

		val titleField = ObservableField("")
		val subtitleField = ObservableField("")

		init {
			binding.setVariable(BR.message, this)
		}

		override fun bind(data: Message) {
			super.bind(data)
			titleField.set(data.text)
			subtitleField.set(data.info)
		}

		fun onItemClick() = listener?.onItemClick()
	}

	class ItemViewHolder(binding: ViewDataBinding)
		: ViewHolder(binding) {

		interface Listener {
			fun onImageClick(uri: Uri)
		}

		var listener: Listener? = null

		val textField = ObservableField("")
		val imageUri = ObservableField(Uri.EMPTY)

		init {
			binding.setVariable(BR.message, this)
		}

		override fun bind(data: Message) {
			super.bind(data)
			textField.set(data.text)
			imageUri.set(data.attachUri)
		}

		override fun bind(payload: Bundle) {
			super.bind(payload)

			val newText = payload.getString(DiffCallback.KEY_TEXT)
			if (newText != null)
				textField.set(newText)

			val newImageUrl = payload.getString(DiffCallback.KEY_URL)
			if (newImageUrl != null)
				imageUri.set(Uri.parse(newImageUrl))
		}

		fun onImageClick() = listener?.onImageClick(imageUri.get()!!)
	}

	abstract class ViewHolder(binding: ViewDataBinding)
		: BasicRecyclerAdapter.ViewHolder<Message, ViewDataBinding>(binding) {

		val timestampField = ObservableField("")
		val statusIconRes = ObservableInt()
		val uploadProgress = ObservableInt()
		val progressVisibility = ObservableBoolean()

		@CallSuper
		override fun bind(data: Message) {
			timestampField.set(data.formatedTime)

			statusIconRes.set(getStatusResId(data.status))

			progressVisibility.set(data.progress >= 0)
			uploadProgress.set(data.progress)
		}

		@CallSuper
		open fun bind(payload: Bundle) {
			val newStatus = payload.getInt(DiffCallback.KEY_STATUS, -1)
			if (newStatus != -1)
				statusIconRes.set(getStatusResId(newStatus))

			val progress = payload.getInt(DiffCallback.KEY_PROGRESS, -1)
			if (progress != -1)
				uploadProgress.set(progress)
		}

		@DrawableRes
		private fun getStatusResId(@Message.Status status: Int) : Int {
			return when (status) {
				Message.STATUS_SENT -> R.drawable.ic_send_check
				Message.STATUS_RECEIVED -> R.drawable.ic_send_check_all
				else -> R.drawable.ic_send_wait
			}
		}
	}

	class DiffCallback(private val oldItems: List<Message>,
	                   private val newItems: List<Message>)
		: DiffUtil.Callback() {

		companion object {
			const val KEY_URL = "url"
			const val KEY_TEXT = "text"
			const val KEY_STATUS = "status"
			const val KEY_PROGRESS = "progress"
		}

		override fun getOldListSize() = oldItems.size

		override fun getNewListSize() = newItems.size

		override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos].timestamp == newItems[newPos].timestamp

		override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldItems[oldPos] == newItems[newPos]

		override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
			val oldMessage = oldItems[oldItemPosition]
			val newMessage = newItems[newItemPosition]

			val bundle = Bundle().apply {
				if (newMessage.attachUrl != oldMessage.attachUrl)
					putString(KEY_URL, newMessage.attachUrl)
				if (newMessage.text != oldMessage.text)
					putString(KEY_TEXT, newMessage.text)
				if (newMessage.status != oldMessage.status)
					putInt(KEY_STATUS, newMessage.status)
			}

			return if (bundle.isNotEmpty()) bundle else null
		}
	}

	fun observeFileOpen() = openFile as Observable<Uri>

	fun observeImageOpen() = openImage as Observable<Uri>

	inner class Filter: ListFilter() {
		override fun satisfy(item: Message, candidate: String) = item.contains(candidate)
	}
}
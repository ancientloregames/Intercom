package com.ancientlore.intercom.ui.chat.flow

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import androidx.annotation.DrawableRes
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.App
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.C
import com.ancientlore.intercom.C.INVALID_INDEX
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.ProgressRequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.ListChanges
import com.ancientlore.intercom.databinding.*
import com.ancientlore.intercom.manager.MediaPlayerManager
import com.ancientlore.intercom.manager.MediaPlayerManager.STATUS_PAUSED
import com.ancientlore.intercom.manager.MediaPlayerManager.STATUS_PLAYING
import com.ancientlore.intercom.manager.MediaPlayerManager.STATUS_RELEASED
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.getAudioMessagesDir
import com.ancientlore.intercom.utils.extensions.isNotEmpty
import com.ancientlore.intercom.widget.recycler.BasicRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.MutableRecyclerAdapter
import com.ancientlore.intercom.widget.recycler.HeadedRecyclerDiffUtil
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.lang.RuntimeException
import kotlin.collections.ArrayList

class ChatFlowAdapter(private val userId: String,
                      context: Context,
                      items: MutableList<Message> = mutableListOf())
	: MutableRecyclerAdapter<Message, ChatFlowAdapter.ViewHolder, ViewDataBinding>
		(context, items, autoSort = true) {

	private companion object {
		private const val VIEW_TYPE_USER = 0
		private const val VIEW_TYPE_OTHER = 1
		private const val VIEW_TYPE_FILE_USER = 2
		private const val VIEW_TYPE_FILE_OTHER = 3
		private const val VIEW_TYPE_AUDIO_USER = 4
		private const val VIEW_TYPE_AUDIO_OTHER = 5
	}

	private val openFileSubj = PublishSubject.create<Uri>()

	private val openImageSubj = PublishSubject.create<Uri>()

	private val openOptionMenuSubj = PublishSubject.create<Message>()

	fun clean() {
		openFileSubj.onComplete()
		openImageSubj.onComplete()
		openOptionMenuSubj.onComplete()
	}

	override fun getDiffCallback(newItems: List<Message>) = DiffCallback(getItems(), newItems)

	override fun getItemViewTypeInner(position: Int): Int {
		val message = getItem(position)!!
		return when (message.senderId) {
			userId -> when (message.type) {
				Message.TYPE_FILE -> VIEW_TYPE_FILE_USER
				Message.TYPE_AUDIO -> VIEW_TYPE_AUDIO_USER
				else -> VIEW_TYPE_USER
			}
			else ->  when (message.type) {
				Message.TYPE_FILE -> VIEW_TYPE_FILE_OTHER
				Message.TYPE_AUDIO -> VIEW_TYPE_AUDIO_OTHER
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
			VIEW_TYPE_AUDIO_USER -> ChatFlowAudioUserBinding.inflate(layoutInflater, parent, false)
			VIEW_TYPE_AUDIO_OTHER -> ChatFlowAudioOtherBinding.inflate(layoutInflater, parent, false)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun createItemViewHolder(binding: ViewDataBinding, viewType: Int): ViewHolder {
		return when (viewType) {
			VIEW_TYPE_USER, VIEW_TYPE_OTHER -> ItemViewHolder(binding)
			VIEW_TYPE_FILE_USER, VIEW_TYPE_FILE_OTHER -> FileItemViewHolder(binding)
			VIEW_TYPE_AUDIO_USER, VIEW_TYPE_AUDIO_OTHER -> AudioItemViewHolder(binding)
			else -> throw RuntimeException("Error! Unknown view extension. Check getItemViewType method")
		}
	}

	override fun bindItemViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {

		val item = getItem(position)!!

		holder.longClickListener = object : ViewHolder.LongClickListener {
			override fun onLongClick() {
				openOptionMenuSubj.onNext(item)
			}
		}

		when (holder) {
			is ItemViewHolder -> holder.imageClickListener = object : ItemViewHolder.ImageClickListener {
				override fun onImageClick(uri: Uri) {
					openImageSubj.onNext(uri)
				}
			}
			is FileItemViewHolder -> holder.fileClickListener = object : FileItemViewHolder.FileClickListener {
				override fun onItemClick() {
					openFileSubj.onNext(item.attachUri)
				}
			}
		}

		if (payloads.isNotEmpty())
			holder.bind(payloads[0] as Bundle)
		else
			holder.bind(item)
	}

	@UiThread
	fun applyChanges(changes: ListChanges<Message>) {

		val updatedList = ArrayList(fullList)

		val iter = updatedList.listIterator()
		while (iter.hasNext()) {
			val listItem = iter.next()

			val removeIndex = changes.removeList.indexOfFirst { it.id == listItem.id }
			if (removeIndex != INVALID_INDEX) {
				Log.d("ChatFlow", "remove: ${listItem.text}")
				iter.remove()
				changes.removeList.removeAt(removeIndex)
				continue
			}

			val updateIndex = changes.modifyList.indexOfFirst { it.id == listItem.id }
			if (updateIndex != INVALID_INDEX) {
				Log.d("ChatFlow", "update: ${listItem.text}")
				val updatedItem = changes.modifyList[updateIndex]
				iter.set(updatedItem)
				changes.modifyList.removeAt(updateIndex)
				continue
			}

			if (changes.removeList.isEmpty() && changes.modifyList.isEmpty())
				break
		}

		updatedList.addAll(changes.addList)
		updatedList.addAll(changes.modifyList) // if they weren't removed, they are to be added

		super.setItems(updatedList)
	}

	override fun isTheSame(first: Message, second: Message) = first == second

	override fun isUnique(item: Message) = getItems().none { it.id == item.id }

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

	class AudioItemViewHolder(binding: ViewDataBinding)
		: ViewHolder(binding), MediaPlayerManager.Listener {

		val iconRes = ObservableInt(R.drawable.ic_play)
		val durationField = ObservableField("")
		val seekBarMax = ObservableInt()
		val seekBarValue = ObservableInt()

		private val player = MediaPlayerManager

		private lateinit var filePath: String

		init {
			binding.setVariable(BR.message, this)
		}

		override fun bind(data: Message) {
			super.bind(data)

			manageAttachAudio(data.attachUrl)
		}

		override fun bind(payload: Bundle) {
			super.bind(payload)

			val newAudioUrl = payload.getString(DiffCallback.KEY_URL)
			if (newAudioUrl != null)
				manageAttachAudio(newAudioUrl)
		}

		private fun manageAttachAudio(url: String) {
			if (Utils.isExternalUrl(url)) {
				val filename = Utils.getFileName(url)
				val dir = itemView.context.getAudioMessagesDir()
				val file = File(dir, filename)
				filePath = file.absolutePath

				if (!file.exists()) {
					if (file.createNewFile()) {
						progressVisibility.set(true)

						App.backend.getStorageManager().download(url, file, object : ProgressRequestCallback<Any> {
							override fun onProgress(progress: Int) {
								uploadProgress.set(progress)
							}
							override fun onSuccess(result: Any) {
								onFileReady(file)
							}
							override fun onFailure(error: Throwable) {
								Utils.logError(error)
							}
						})
					}
				} else onFileReady(file)
			} else
				progressVisibility.set(true)
		}

		override fun onComplete() {
			seekBarValue.set(0)
			iconRes.set(R.drawable.ic_play)
		}

		override fun onProgress(progress: Int) {
			seekBarValue.set(progress)
			durationField.set(Utils.getFormatedDuration(progress.toLong()))
		}

		fun onItemClick() {
			when (player.getStatus()) {
				STATUS_RELEASED -> {
					player.prepare(filePath)
					player.setProgress(seekBarValue.get())
					player.setListener(this)
					player.play()
					iconRes.set(R.drawable.ic_pause)
				}
				STATUS_PLAYING -> {
					player.pause()
					iconRes.set(R.drawable.ic_play)
				}
				STATUS_PAUSED -> {
					player.play()
					iconRes.set(R.drawable.ic_pause)
				}
			}
		}

		fun seekBarListener(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
			if (fromUser) {
				player.setProgress(progresValue)
			}
		}

		private fun onFileReady(file: File) {
			val duration = Utils.getDuration(file)
			seekBarMax.set(duration.toInt())

			durationField.set(Utils.getFormatedDuration(duration))

			progressVisibility.set(false)
		}
	}

	class FileItemViewHolder(binding: ViewDataBinding)
		: ViewHolder(binding) {

		interface FileClickListener {
			fun onItemClick()
		}

		var fileClickListener: FileClickListener? = null

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

		fun onItemClick() = fileClickListener?.onItemClick()
	}

	class ItemViewHolder(binding: ViewDataBinding)
		: ViewHolder(binding) {

		interface ImageClickListener {
			fun onImageClick(uri: Uri)
		}

		var imageClickListener: ImageClickListener? = null

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

		fun onImageClick() = imageClickListener?.onImageClick(imageUri.get()!!)
	}

	abstract class ViewHolder(binding: ViewDataBinding)
		: BasicRecyclerAdapter.ViewHolder<Message, ViewDataBinding>(binding) {

		interface LongClickListener {
			fun onLongClick()
		}

		var longClickListener: LongClickListener? = null

		val timestampField = ObservableField("")
		val statusIconRes = ObservableInt()
		val uploadProgress = ObservableInt(100)
		val progressVisibility = ObservableBoolean()

		fun onLongClick() : Boolean {
			longClickListener?.onLongClick()
			return true
		}

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
		: HeadedRecyclerDiffUtil.Callback() {

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
			Log.d(C.DEFAULT_LOG_TAG, "getChangePayload: ${oldMessage.status} to ${newMessage.status}")

			val bundle = Bundle().apply {
				if (newMessage.attachUrl != oldMessage.attachUrl)
					putString(KEY_URL, newMessage.attachUrl)
				if (newMessage.text != oldMessage.text)
					putString(KEY_TEXT, newMessage.text)
				if (newMessage.status > oldMessage.status)
					putInt(KEY_STATUS, newMessage.status)
			}

			return if (bundle.isNotEmpty()) bundle else null
		}
	}

	fun fileOpenRequest() = openFileSubj as Observable<Uri>

	fun imageOpenRequest() = openImageSubj as Observable<Uri>

	fun optionMenuOpenRequest() = openOptionMenuSubj as Observable<Message>

	inner class Filter: ListFilter() {
		override fun satisfy(item: Message, candidate: String) = item.contains(candidate)
	}
}
package com.ancientlore.intercom.ui.chat.flow

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.ChatFlowUiBinding
import com.ancientlore.intercom.dialog.bottomsheet.list.ListBottomSheetDialog
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.ui.dialog.attach.AttachBottomSheetDialog
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.Runnable1
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.enableChatBehavior
import com.ancientlore.intercom.utils.extensions.getAppCacheDir
import com.ancientlore.intercom.utils.extensions.getFileData
import com.ancientlore.intercom.utils.extensions.openFile
import com.ancientlore.intercom.widget.list.simple.SimpleListItem
import kotlinx.android.synthetic.main.chat_flow_ui.*
import kotlinx.android.synthetic.main.chat_flow_ui.listView
import kotlinx.android.synthetic.main.chat_flow_ui.toolbar
import java.io.File

class ChatFlowFragment : BasicFragment<ChatFlowViewModel, ChatFlowUiBinding>() {

	companion object {
		const val INTENT_GET_FILES = 101
		const val INTENT_GET_IMAGES = 102

		private const val ARG_CHAT_ID = "chat_id"
		private const val ARG_CHAT_TITLE = "chat_title"

		fun newInstance(params: Params) : ChatFlowFragment {
			return ChatFlowFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_CHAT_ID, params.chatId)
					putString(ARG_CHAT_TITLE, params.title)
				}
			}
		}
	}

	data class Params(val chatId: String, val title: String)

	private val chatId get() = arguments?.getString(ARG_CHAT_ID)
		?: throw RuntimeException("Chat id is a mandotory arg")

	private val title get() = arguments?.getString(ARG_CHAT_TITLE)
		?: throw RuntimeException("Chat title is a mandotory arg")

	private val userId get() = App.backend.getAuthManager().getCurrentUser()?.id
		?: throw RuntimeException("This fragment may be created only after successful authorization")

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getLayoutResId() = R.layout.chat_flow_ui

	override fun createViewModel() = ChatFlowViewModel(userId, chatId)

	override fun bind(view: View, viewModel: ChatFlowViewModel) {
		dataBinding = ChatFlowUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		initToolbarMenu()
		ToolbarManager(toolbar as Toolbar).apply {
			setTitle(title)
			enableBackButton(View.OnClickListener {
				close()
			})
		}

		swipableLayout.setListener { close() }

		with(listView) {
			adapter = ChatFlowAdapter(userId, context!!, mutableListOf())
			enableChatBehavior()
		}
	}

	private fun initToolbarMenu() {
		navigator?.createToolbarMenu(toolbar, Runnable1 { menu ->
			activity?.menuInflater?.inflate(R.menu.chat_flow_menu, menu)
			val search = menu.findItem(R.id.search).actionView as SearchView
			search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
				override fun onQueryTextSubmit(query: String?): Boolean {
					query?.let { constraint ->
						viewModel.filter(constraint)
					}
					return true
				}
				override fun onQueryTextChange(newText: String?): Boolean {
					newText
						?.takeIf { it.length > 1 }
						?.let { viewModel.filter(it) }
						?:run { viewModel.filter("") }
					return true
				}
			})
		})
	}

	override fun initViewModel(viewModel: ChatFlowViewModel) {
		val listAdapter = listView.adapter as ChatFlowAdapter
		subscriptions.add(listAdapter.observeFileOpen()
			.subscribe {
				context?.openFile(it)
			})
		subscriptions.add(listAdapter.observeImageOpen()
			.subscribe {
				// TODO create custom image viewer fragment
				context?.openFile(it)
			})
		viewModel.setListAdapter(listAdapter)
	}

	override fun observeViewModel(viewModel: ChatFlowViewModel) {
		subscriptions.add(viewModel.observeToastRequest()
			.subscribe { showToast(it) })
		subscriptions.add(viewModel.observeAttachMenuOpen()
			.subscribe { openAttachMenu() })
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		val handled = if (resultCode == RESULT_OK && data != null)
			handleActivityResult(requestCode, data)
		else {
			showToast(R.string.alert_error_attach_file)
			false
		}

		if (!handled)
			super.onActivityResult(requestCode, resultCode, data)
	}

	private fun handleActivityResult(requestCode: Int, data: Intent) : Boolean {
		return when (requestCode) {
			INTENT_GET_IMAGES -> {
				val uri = getIntentResult(data)
				val fileData = uri.getFileData(context!!.contentResolver)
				val file = File(context!!.getAppCacheDir(), fileData.name)

				if (!file.exists() && file.createNewFile())
					ImageUtils.compressImage(context!!.contentResolver, uri, C.MAX_ATTACH_IMG_SIZE_PX, file)

				if (file.exists()) {
					viewModel.handleAttachedImage(fileData, Uri.fromFile(file))
				} else Utils.logError(RuntimeException("Failed to create"))
				true
			}
			INTENT_GET_FILES -> {
				val uri = getIntentResult(data)
				val fileData = uri.getFileData(context!!.contentResolver)
				viewModel.handleAttachedFile(fileData)
				true
			}
			else -> false
		}
	}

	private fun getIntentResult(intent: Intent) : Uri {
		// TODO multiple selection case (clipData)
		return intent.data ?: Uri.EMPTY
	}

	private fun openAttachMenu() {
		AttachBottomSheetDialog.newInstance().apply {
			setListener(object : ListBottomSheetDialog.Listener {
				override fun onItemSelected(item: SimpleListItem) {
					onAttachMenuItemSelected(item.id)
				}
			})
		}.show(fragmentManager!!)
	}

	private fun onAttachMenuItemSelected(id: Int) {
		when (id) {
			R.id.im_attach_picture -> openGallery()
			R.id.im_attach_file -> openFilePicker()
			else -> {
				showToast(R.string.error)
				Utils.logError(RuntimeException("Error! Unknown attach source"))
			}
		}
	}

	private fun openGallery() {
		permissionManager?.requestPermissionWriteStorage(Runnable1 { granted ->
			if (granted) {
				// FIXME temporary solution (TODO make own gallery)
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("image/*")
				// TODO multiple selection .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
				startActivityForResult(intent, INTENT_GET_IMAGES)
			}
		})
	}

	private fun openFilePicker() {
		permissionManager?.requestPermissionReadStorage(Runnable1 { granted ->
			if (granted) {
				val intent = Intent(Intent.ACTION_GET_CONTENT)
					.setType("*/*")
				// TODO multiple selection .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
				startActivityForResult(intent, INTENT_GET_FILES)
			}
		})
	}
}

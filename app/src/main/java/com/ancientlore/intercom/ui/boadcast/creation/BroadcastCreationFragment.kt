package com.ancientlore.intercom.ui.boadcast.creation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.BroadcastCreationUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import javax.inject.Inject

class BroadcastCreationFragment
	: BasicFragment<BroadcastCreationViewModel, BroadcastCreationUiBinding>() {

	companion object {
		const val INTENT_GET_IMAGE = 101

		fun newInstance() = BroadcastCreationFragment()
	}

	@Inject
	protected lateinit var viewModel: BroadcastCreationViewModel

	override fun getLayoutResId(): Int = R.layout.broadcast_creation_ui

	override fun createDataBinding(view: View) = BroadcastCreationUiBinding.bind(view)

	override fun requestViewModel() = BroadcastCreationViewModel()

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		dataBinding.ui = viewModel

		ToolbarManager(dataBinding.toolbar).apply {
			enableBackButton { close() }
		}

		dataBinding.swipableLayout.setListener { close(false) }

		subscriptions.addAll(
			viewModel.pickIconRequest().subscribe {
				navigator?.openImagePicker(this, INTENT_GET_IMAGE)
			},
			viewModel.createBroadcastRequest().subscribe {
				navigator?.openChatFlow(it)
			})
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

		when (requestCode) {
			INTENT_GET_IMAGE -> {
				if (resultCode == Activity.RESULT_OK && intent != null) {
					if (intent.data != null && intent.data != Uri.EMPTY) {
						viewModel.onIconPicked(intent.data)
					}
					else {
						Utils.logError("ChatCreationBroadcastFragment.handleGetImageIntent(): No data in the intent")
						showToast(R.string.alert_error_set_photo)
					}
				}
			}
			else -> super.onActivityResult(requestCode, resultCode, intent)
		}
	}
}
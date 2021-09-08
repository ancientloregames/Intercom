package com.ancientlore.intercom.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.SettingsUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.ToolbarManager
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.getAppCacheDir
import com.ancientlore.intercom.utils.extensions.getFileData
import java.io.File

class SettingsFragment : BasicFragment<SettingsViewModel, SettingsUiBinding>()  {

	companion object {
		const val INTENT_GET_IMAGE = 101

		fun newInstance() = SettingsFragment()
	}

	override fun getOpenAnimation(): Int = R.anim.slide_in_bottom

	override fun getCloseAnimation(): Int = R.anim.slide_out_bottom

	override fun getLayoutResId() = R.layout.settings_ui

	override fun createDataBinding(view: View) = SettingsUiBinding.bind(view)

	override fun createViewModel() = SettingsViewModel(App.backend.getAuthManager().getCurrentUser())

	override fun init(viewModel: SettingsViewModel, savedState: Bundle?) {
		super.init(viewModel, savedState)

		dataBinding.ui = viewModel

		navigator
			?.apply {
				createToolbarMenu(dataBinding.toolbar)
				ToolbarManager(dataBinding.toolbar).apply {
					enableBackButton { close() }
				}
			}

		dataBinding.swipableLayout.setListener { close(false) }

		viewModel.init(context!!)

		subscriptions.add(viewModel.observeOpenGalleryRequest()
			.subscribe { openGallery() })

		subscriptions.add(viewModel.openImageViewerRequest()
			.subscribe { navigator?.openImageViewer(it) })
	}

	override fun onDestroyView() {
		dataBinding.toolbar.setNavigationOnClickListener(null)
		dataBinding.swipableLayout.setListener(null)
		super.onDestroyView()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		val handled = if (resultCode == Activity.RESULT_OK && data != null)
			handleActivityResult(requestCode, data)
		else {
			showToast(R.string.alert_error_set_photo)
			false
		}

		if (!handled)
			super.onActivityResult(requestCode, resultCode, data)
	}

	private fun handleActivityResult(requestCode: Int, intent: Intent) : Boolean {

		return context?.let { context ->

			when (requestCode) {

				INTENT_GET_IMAGE -> {
					if (intent.data != null) {

						val imageUri = intent.data
						val fileData = imageUri.getFileData(context.contentResolver)
						val file = File(context.getAppCacheDir(), fileData.name)

						if (!file.exists() && file.createNewFile())
							ImageUtils.compressImage(context.contentResolver, imageUri, C.MAX_ATTACH_IMG_SIZE_PX, file)

						if (file.exists()) {
							viewModel.handleSelectedProfileIcon(fileData)
						}
						else {
							Utils.logError("SettingsFragment.handleActivityResult(): Failed to create file")
							showToast(R.string.alert_error_set_photo)
						}
					}
					else {
						Utils.logError("SettingsFragment.handleActivityResult(): No data in the intent: $requestCode")
						showToast(R.string.alert_error_set_photo)
					}
					true
				}
				else -> false
			}
		} ?: true
	}

	private fun openGallery() {
		permissionManager?.requestPermissionWriteStorage { granted ->
			if (granted) {
				// FIXME temporary solution (TODO make own gallery)
				activity
					?.let {
					val intent = Intent(Intent.ACTION_GET_CONTENT)
						.setType("image/*")
					if (intent.resolveActivity(it.packageManager) != null)
						startActivityForResult(intent, INTENT_GET_IMAGE)
					else {
						Utils.logError("SettingsFragment.openGallery(): Device has no gallery")
						showToast(R.string.alert_error_no_gallery)
					}
				}
			}
		}
	}
}
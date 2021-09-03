package com.ancientlore.intercom.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
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
import kotlinx.android.synthetic.main.settings_ui.*
import java.io.File

class SettingsFragment : BasicFragment<SettingsViewModel, SettingsUiBinding>()  {

	companion object {
		const val INTENT_GET_IMAGE = 101

		fun newInstance() = SettingsFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getOpenAnimation(): Int = R.anim.slide_in_bottom

	override fun getCloseAnimation(): Int = R.anim.slide_out_bottom

	override fun getLayoutResId() = R.layout.settings_ui

	override fun createViewModel() = SettingsViewModel(App.backend.getAuthManager().getCurrentUser())

	override fun bind(view: View, viewModel: SettingsViewModel) {
		dataBinding = SettingsUiBinding.bind(view)
		dataBinding.ui = viewModel
	}

	override fun initViewModel(viewModel: SettingsViewModel) {
		context?.run {
			viewModel.init(this)
		}
	}

	override fun observeViewModel(viewModel: SettingsViewModel) {
		super.observeViewModel(viewModel)

		subscriptions.add(viewModel.observeOpenGalleryRequest()
			.subscribe { openGallery() })
	}

	override fun initView(view: View, savedInstanceState: Bundle?) {
		navigator
			?.apply {
				createToolbarMenu(toolbar)
				ToolbarManager(toolbar as Toolbar).apply {
					enableBackButton { close() }
				}
			}

		swipableLayout.setListener { close(false) }
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
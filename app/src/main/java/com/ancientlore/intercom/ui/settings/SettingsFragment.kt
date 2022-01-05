package com.ancientlore.intercom.ui.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
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
import com.ancientlore.intercom.utils.extensions.showKeyboard
import java.io.File
import android.content.ComponentName
import android.net.Uri


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

		val context = context!!

		dataBinding.ui = viewModel

		navigator
			?.apply {
				createToolbarMenu(dataBinding.toolbar)
				ToolbarManager(dataBinding.toolbar).apply {
					enableBackButton { close() }
				}
			}

		dataBinding.swipableLayout.setListener { close(false) }

		val editNameView = EditText(context).apply {
			setHint(R.string.dialog_edit_user_name_hint)
		}
		val editNameDialog = AlertDialog.Builder(context)
			.setTitle(R.string.dialog_edit_user_name_title)
			.setMessage(R.string.dialog_edit_user_name_message)
			.setView(editNameView)
			.setPositiveButton(R.string.ok) { _, _ ->
				viewModel.updateUserName(
					editNameView.text.toString())
			}
			.setNegativeButton(R.string.cancel, null)
			.create()

		val editStatusView = EditText(context).apply {
			setHint(R.string.dialog_edit_user_name_hint)
		}
		val editStatusDialog = AlertDialog.Builder(context)
			.setTitle(R.string.dialog_edit_user_status_title)
			.setMessage(R.string.dialog_edit_user_status_message)
			.setView(editStatusView)
			.setPositiveButton(R.string.ok) { _, _ ->
				viewModel.updateUserStatus(
					editStatusView.text.toString())
			}
			.setNegativeButton(R.string.cancel, null)
			.create()

		subscriptions.add(viewModel.observeOpenGalleryRequest()
			.subscribe {
				navigator?.openImagePicker(this, INTENT_GET_IMAGE)
			})

		subscriptions.add(viewModel.openImageViewerRequest()
			.subscribe { navigator?.openImageViewer(it) })

		subscriptions.add(viewModel.showNameEditorRequest()
			.subscribe {
				editNameView.setText(it)
				editNameDialog?.show()
				Utils.runOnUiThread({ editNameView.showKeyboard() }, 20)
			})

		subscriptions.add(viewModel.showStatusEditorRequest()
			.subscribe {
				editStatusView.setText(it)
				editStatusDialog?.show()
				Utils.runOnUiThread({ editStatusView.showKeyboard() }, 20)
			})
		subscriptions.add(viewModel.openAppSettingsRequest()
			.subscribe {
				val intent = Intent().apply {
					action = "android.settings.APPLICATION_DETAILS_SETTINGS"
					data = Uri.parse("package:${context.packageName}")
					component = ComponentName(
						"com.android.settings",
						"com.android.settings.applications.InstalledAppDetails")
				}
				startActivity(intent)
			})
		subscriptions.add(viewModel.openNotificationSettingsRequest()
			.subscribe {
				val intent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
					.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
				startActivity(intent)
			})
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

	override fun getToastStringRes(toastId: Int): Int {
		return when (toastId) {
			SettingsViewModel.TOAST_INVALID_NAME -> R.string.alert_error_invalid_name
			SettingsViewModel.TOAST_INVALID_STATUS -> R.string.alert_error_invalid_user_status
			SettingsViewModel.TOAST_ERR_CHANGE_NAME -> R.string.alert_error_change_name
			SettingsViewModel.TOAST_ERR_CHANGE_STATUS -> R.string.alert_error_change_user_status
			else -> super.getToastStringRes(toastId)
		}
	}
}
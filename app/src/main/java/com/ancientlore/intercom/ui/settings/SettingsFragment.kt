package com.ancientlore.intercom.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ancientlore.intercom.R
import com.ancientlore.intercom.databinding.SettingsUiBinding
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.utils.ToolbarManager
import kotlinx.android.synthetic.main.settings_ui.*

class SettingsFragment : BasicFragment<SettingsViewModel, SettingsUiBinding>()  {

	companion object {
		fun newInstance() = SettingsFragment()
	}

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	override fun getLayoutResId() = R.layout.settings_ui

	override fun createViewModel() = SettingsViewModel()

	override fun bind(view: View, viewModel: SettingsViewModel) {
		dataBinding = SettingsUiBinding.bind(view)
	}

	override fun initViewModel(viewModel: SettingsViewModel) {
		//TODO
	}

	override fun observeViewModel(viewModel: SettingsViewModel) {
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

		swipableLayout.setListener { close() }
	}

	private fun openGallery() {
		// TODO
	}
}
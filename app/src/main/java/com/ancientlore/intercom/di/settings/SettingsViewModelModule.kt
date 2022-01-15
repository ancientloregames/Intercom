package com.ancientlore.intercom.di.settings

import androidx.lifecycle.ViewModel
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SettingsViewModelModule {

	@SettingsScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(SettingsViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: SettingsViewModel): ViewModel
}
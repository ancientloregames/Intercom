package com.ancientlore.intercom.di.auth.phone.check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface PhoneCheckViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@PhoneCheckScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(PhoneCheckViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: PhoneCheckViewModel): ViewModel
}
package com.ancientlore.intercom.di.auth.phone.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.auth.phone.login.PhoneLoginViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface PhoneLoginViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@PhoneLoginScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(PhoneLoginViewModel::class)
	fun bindPhoneLoginViewModel(viewModel: PhoneLoginViewModel): ViewModel
}
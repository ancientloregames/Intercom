package com.ancientlore.intercom.di.auth.email.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.auth.email.login.EmailLoginViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface EmailLoginViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@EmailLoginScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(EmailLoginViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: EmailLoginViewModel): ViewModel
}
package com.ancientlore.intercom.di.auth.email.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.auth.email.signup.EmailSignupViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface EmailSignupViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@EmailSignupScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(EmailSignupViewModel::class)
	fun bindEmailSignupViewModel(viewModel: EmailSignupViewModel): ViewModel
}
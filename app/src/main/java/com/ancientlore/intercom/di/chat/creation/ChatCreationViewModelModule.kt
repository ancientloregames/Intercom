package com.ancientlore.intercom.di.chat.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.chat.creation.ChatCreationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ChatCreationViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@ChatCreationScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ChatCreationViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ChatCreationViewModel): ViewModel
}
package com.ancientlore.intercom.di.chat.creation.group.desc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ChatCreationDescViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@ChatCreationDescScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ChatCreationDescViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ChatCreationDescViewModel): ViewModel
}
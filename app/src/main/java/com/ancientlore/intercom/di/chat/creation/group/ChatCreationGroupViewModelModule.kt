package com.ancientlore.intercom.di.chat.creation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.chat.creation.group.ChatCreationGroupViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ChatCreationGroupViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@ChatCreationGroupScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ChatCreationGroupViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ChatCreationGroupViewModel): ViewModel
}
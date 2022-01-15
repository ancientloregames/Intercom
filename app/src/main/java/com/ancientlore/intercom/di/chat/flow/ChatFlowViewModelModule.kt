package com.ancientlore.intercom.di.chat.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.chat.flow.ChatFlowViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ChatFlowViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@ChatFlowScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ChatFlowViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ChatFlowViewModel): ViewModel
}
package com.ancientlore.intercom.di.chat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.chat.detail.ChatDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ChatDetailViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@ChatDetailScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ChatDetailViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ChatDetailViewModel): ViewModel
}
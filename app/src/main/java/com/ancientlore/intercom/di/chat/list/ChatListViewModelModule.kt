package com.ancientlore.intercom.di.chat.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.chat.list.ChatListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ChatListViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@ChatListScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ChatListViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ChatListViewModel): ViewModel
}
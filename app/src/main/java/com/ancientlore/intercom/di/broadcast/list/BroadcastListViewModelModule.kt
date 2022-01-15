package com.ancientlore.intercom.di.broadcast.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.boadcast.list.BroadcastListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface BroadcastListViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@BroadcastListScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(BroadcastListViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: BroadcastListViewModel): ViewModel
}
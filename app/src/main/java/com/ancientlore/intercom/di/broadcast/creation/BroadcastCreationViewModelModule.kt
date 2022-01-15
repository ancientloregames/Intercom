package com.ancientlore.intercom.di.broadcast.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ancientlore.intercom.di.ViewModelFactory
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.boadcast.creation.BroadcastCreationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface BroadcastCreationViewModelModule {

	@Binds
	fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

	@BroadcastCreationScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(BroadcastCreationViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: BroadcastCreationViewModel): ViewModel
}
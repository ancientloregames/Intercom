package com.ancientlore.intercom.di.contact.list

import androidx.lifecycle.ViewModel
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.contact.list.ContactListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ContactListViewModelModule {

	@ContactListScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ContactListViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ContactListViewModel): ViewModel
}
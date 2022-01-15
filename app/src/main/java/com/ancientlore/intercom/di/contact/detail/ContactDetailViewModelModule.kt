package com.ancientlore.intercom.di.contact.detail

import androidx.lifecycle.ViewModel
import com.ancientlore.intercom.di.ViewModelKey
import com.ancientlore.intercom.ui.contact.detail.ContactDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ContactDetailViewModelModule {

	@ContactDetailScreenScope
	@Binds
	@IntoMap
	@ViewModelKey(ContactDetailViewModel::class)
	fun bindPhoneCheckViewModel(viewModel: ContactDetailViewModel): ViewModel
}
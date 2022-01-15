package com.ancientlore.intercom.di.contact.detail

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.contact.detail.ContactDetailFragment
import com.ancientlore.intercom.ui.contact.detail.ContactDetailParams
import dagger.Module
import dagger.Provides

@Module
class ContactDetailModule {

	@ContactDetailScreenScope
	@Provides
	fun getParams(fragment: ContactDetailFragment): ContactDetailParams {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}
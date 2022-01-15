package com.ancientlore.intercom.di.chat.creation.group.desc

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescFragment
import com.ancientlore.intercom.ui.chat.creation.description.ChatCreationDescViewModel
import dagger.Module
import dagger.Provides

@Module
object ChatCreationDescModule {

	@ChatCreationDescScreenScope
	@Provides
	fun getParams(fragment: ChatCreationDescFragment): ChatCreationDescViewModel.Params {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}
package com.ancientlore.intercom.di.chat.detail

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.chat.detail.ChatDetailFragment
import com.ancientlore.intercom.ui.chat.detail.ChatDetailViewModel
import dagger.Module
import dagger.Provides

@Module
class ChatDetailModule {

	@ChatDetailScreenScope
	@Provides
	fun getParams(fragment: ChatDetailFragment): ChatDetailViewModel.Params {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}
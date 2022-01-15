package com.ancientlore.intercom.di.chat.flow

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.chat.flow.ChatFlowFragment
import com.ancientlore.intercom.ui.chat.flow.ChatFlowParams
import dagger.Module
import dagger.Provides

@Module
class ChatFlowModule {

	@ChatFlowScreenScope
	@Provides
	fun getParams(fragment: ChatFlowFragment): ChatFlowParams {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}
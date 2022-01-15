package com.ancientlore.intercom.di.auth.phone.check

import com.ancientlore.intercom.C.ARG_FRAGMENT_PARAMS
import com.ancientlore.intercom.backend.auth.PhoneAuthParams
import com.ancientlore.intercom.di.NoFragmentParamsException
import com.ancientlore.intercom.ui.auth.phone.check.PhoneCheckFragment
import dagger.Module
import dagger.Provides

@Module
class PhoneCheckModule {

	@PhoneCheckScreenScope
	@Provides
	fun getParams(fragment: PhoneCheckFragment): PhoneAuthParams {
		return fragment.arguments?.getParcelable(ARG_FRAGMENT_PARAMS)
			?: throw NoFragmentParamsException
	}
}
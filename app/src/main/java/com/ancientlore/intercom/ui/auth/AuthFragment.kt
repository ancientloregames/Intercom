package com.ancientlore.intercom.ui.auth

import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.App
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.User
import com.ancientlore.intercom.ui.BasicFragment
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.Utils

abstract class AuthFragment<VM : BasicViewModel, B : ViewDataBinding> : BasicFragment<VM, B>() {

	protected val auth get() = App.backend.getAuthManager()

	abstract fun getAlertMessage(alertCode: Int): String

	protected fun onSuccessfulAuth(user: User) {
		navigator?.onSuccessfullAuth(user)
	}

	protected fun onFailedAuth(error: Throwable) {
		Utils.logError(error)
		showToast(getString(R.string.auth_failure_msg))
	}

	protected fun showAlert(alertCode: Int) {
		showToast(getAlertMessage(alertCode))
	}
}
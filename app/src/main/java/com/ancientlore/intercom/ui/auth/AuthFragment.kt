package com.ancientlore.intercom.ui.auth

import android.widget.Toast
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
		showAlert(getString(R.string.auth_failure_msg))
	}

	protected fun showAlert(alertCode: Int) {
		showAlert(getAlertMessage(alertCode))
	}

	protected fun showAlert(message: String) {
		Utils.runOnUiThread {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
		}
	}
}
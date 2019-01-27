package com.ancientlore.intercom.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.ancientlore.intercom.R
import com.ancientlore.intercom.backend.auth.User
import com.ancientlore.intercom.ui.BasicFragment
import java.lang.RuntimeException

abstract class AuthFragment<VM : ViewModel, B : ViewDataBinding> : BasicFragment<VM, B>() {

	protected val auth get() = backend.getAuthManager()

	abstract fun getAlertMessage(alertCode: Int): String

	protected fun onSuccessfulAuth(user: User) {
		navigator?.onSuccessfullAuth(user)
	}

	override fun onAttach(context: Context) {
		if (context !is AuthNavigator)
			RuntimeException("Context must implement the AuthNavigator interface!")
		super.onAttach(context)
	}

	protected fun onFailedAuth(error: Throwable) {
		error.printStackTrace()
		showAlert(getString(R.string.auth_failure_msg))
	}

	protected fun showAlert(alertCode: Int) {
		showAlert(getAlertMessage(alertCode))
	}

	protected fun showAlert(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
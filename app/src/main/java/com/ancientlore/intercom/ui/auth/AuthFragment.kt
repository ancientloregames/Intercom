package com.ancientlore.intercom.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.ancientlore.intercom.backend.auth.AuthManager
import com.ancientlore.intercom.ui.BasicFragment
import java.lang.RuntimeException

abstract class AuthFragment<VM : ViewModel, B : ViewDataBinding> : BasicFragment<VM, B>() {

	protected val auth get() = backend.getAuthManager()

	protected val navigator: AuthNavigator? get() = activity as AuthNavigator

	abstract fun getAlertMessage(alertCode: Int): String

	protected fun onSuccessfulAuth(user: AuthManager.User) {
		navigator?.onSuccessfullAuth(user)
	}

	override fun onAttach(context: Context) {
		if (context !is AuthNavigator)
			RuntimeException("Context must implement the AuthNavigator interface!")
		super.onAttach(context)
	}

	protected fun onFailedAuth(error: Throwable) {
		error.printStackTrace()
		//TODO show toast with error message
	}

	protected fun showAlert(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
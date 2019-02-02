package com.ancientlore.intercom.ui.auth

import com.ancientlore.intercom.App
import com.ancientlore.intercom.ui.BasicViewModel

abstract class AuthViewModel : BasicViewModel() {

	val authManager get() = App.backend.getAuthManager()

}
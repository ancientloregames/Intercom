package com.ancientlore.intercom.backend.auth

import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.User

interface AuthCallback: RequestCallback<User> {
	fun onVerification(id: String)
}
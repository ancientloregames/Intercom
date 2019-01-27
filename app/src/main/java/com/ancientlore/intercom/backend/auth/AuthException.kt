package com.ancientlore.intercom.backend.auth

class AuthException(message: String = defaultMessage)
	: Throwable(message) {

	companion object {
		private const val defaultMessage = "Some unknown exception has accured during the authorization"
	}
}
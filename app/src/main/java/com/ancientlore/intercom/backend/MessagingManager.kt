package com.ancientlore.intercom.backend

interface MessagingManager {
	fun getToken(callback: RequestCallback<String>)
}
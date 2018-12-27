package com.ancientlore.intercom.utils

interface PermissionManager {
	fun requestContacts(onResult: Runnable1<Boolean>)
}
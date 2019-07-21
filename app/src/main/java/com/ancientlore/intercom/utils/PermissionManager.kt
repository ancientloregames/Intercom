package com.ancientlore.intercom.utils

interface PermissionManager {
	fun requestPermissionReadContacts(onResult: Runnable1<Boolean>)
	fun requestPermissionReadStorage(onResult: Runnable1<Boolean>)
}
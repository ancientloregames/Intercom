package com.ancientlore.intercom.utils

interface PermissionManager {
	fun requestContacts(onResult: Runnable1<Boolean>)
	fun requestPermissionReadStorage(onResult: Runnable1<Boolean>)
}
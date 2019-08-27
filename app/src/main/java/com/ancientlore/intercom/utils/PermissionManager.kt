package com.ancientlore.intercom.utils

interface PermissionManager {
	fun requestPermissionReadContacts(onResult: Runnable1<Boolean>)
	fun requestPermissionReadStorage(onResult: Runnable1<Boolean>)
	fun requestPermissionWriteStorage(onResult: Runnable1<Boolean>)
	fun requestPermissionAudioMessage(onResult: Runnable1<Boolean>)

	fun allowedAudioMessage() : Boolean
}
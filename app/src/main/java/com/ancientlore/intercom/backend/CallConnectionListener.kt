package com.ancientlore.intercom.backend

interface CallConnectionListener {
	fun onConnected()
	fun onDisconnected()
}
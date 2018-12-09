package com.ancientlore.intercom.backend

interface BackendManager {
	fun getBackend(): BackendFactory
}
package com.ancientlore.intercom.backend

interface ProgressRequestCallback<T> : RequestCallback<T> {
	fun onProgress(progress: Int)
}
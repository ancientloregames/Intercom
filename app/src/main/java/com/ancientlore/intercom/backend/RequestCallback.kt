package com.ancientlore.intercom.backend

interface RequestCallback<T> {
	fun onSuccess(result: T)
	fun onFailure(error: Throwable)
}
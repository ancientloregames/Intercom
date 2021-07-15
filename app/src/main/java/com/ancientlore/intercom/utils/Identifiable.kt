package com.ancientlore.intercom.utils

interface Identifiable<T> {

	fun getIdentity(): T // naming is inconvenient, but localId is reserved for Room and id is being  in use across project
}
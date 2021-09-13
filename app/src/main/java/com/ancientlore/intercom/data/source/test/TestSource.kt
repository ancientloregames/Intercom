package com.ancientlore.intercom.data.source.test

import java.util.*
import kotlin.collections.ArrayList

abstract class TestSource {

	companion object {
		const val userListSize = 100

		val testUserIds: ArrayList<String> by lazy { ArrayList<String>(userListSize).apply {
			for (i in 0..userListSize) {
				add(i.toString())
			}
		} }

		val testCurrentUserId: String by lazy {
			Random().nextInt(userListSize).toString()
		}
	}
}
package com.ancientlore.intercom.data.model

data class Chat(val timestamp: Long = 0,
                val participants: Array<String> = emptyArray()) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Chat

		return timestamp == other.timestamp
				&& participants.contentEquals(other.participants)
	}

	override fun hashCode(): Int {
		var result = timestamp.hashCode()
		result = 31 * result + participants.contentHashCode()
		return result
	}
}

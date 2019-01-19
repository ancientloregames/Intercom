package com.ancientlore.intercom.data.model

data class Message(val timestamp: Long = 0,
                   val senderId: String = "",
                   val text: String = "")
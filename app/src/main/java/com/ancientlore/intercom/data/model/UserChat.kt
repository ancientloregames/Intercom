package com.ancientlore.intercom.data.model

import java.util.*

class UserChat(val id: String = "",
               val name: String = "",
               val iconUrl: String = "",
               val lastMsgTime: Date = Date(0),
               val lastMsgText: String = "",
               val pin: Boolean? = null,
               val mute: Boolean? = null) {
}
package com.ancientlore.intercom.data.model

data class User(val alias: String = "",
                val name: String = "",
                val phone: String = "",
                val chats: Map<String, String> = emptyMap())
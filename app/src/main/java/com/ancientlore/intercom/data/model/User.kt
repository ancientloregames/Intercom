package com.ancientlore.intercom.data.model

import com.google.firebase.firestore.Exclude

data class User(val name: String = "",
                val phone: String = "",
                val email: String = "",
                val iconUrl: String = "",
                @get:Exclude val dummy: Boolean = false) {

  @get:Exclude
  val id get() = phone
}
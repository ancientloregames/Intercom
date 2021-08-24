package com.ancientlore.intercom.data.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ancientlore.intercom.utils.Identifiable
import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.util.*

@Entity(tableName = "users")
data class User(@field:ColumnInfo var name: String = "",
                @field:PrimaryKey var phone: String = "",
                @field:ColumnInfo var email: String = "",
                @field:ColumnInfo var iconUrl: String = "",
                @field:ColumnInfo var status: String = "",
                @field:ColumnInfo var lastSeenTime: Date? = null,
                @field:ColumnInfo var online: Boolean = false,
                @field:ColumnInfo var token: String = "",
                @field:Ignore @get:Exclude val dummy: Boolean = false)
  : Identifiable<String> {

  companion object {
    private val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
  }

  @get:Exclude @get:Ignore
  val id get() = phone

  @delegate:Exclude @delegate:Ignore @get:Exclude @get:Ignore
  val iconUri: Uri by lazy { Uri.parse(iconUrl) }

  @delegate:Exclude @delegate:Ignore @get:Exclude @get:Ignore
  val lastSeenDate: String by lazy { if (lastSeenTime != null) dateFormat.format(lastSeenTime) else "" }

  @Exclude @Ignore
  override fun getIdentity() = phone
}
package com.ancientlore.intercom.data.model.crypto

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "signalKeys")
data class SignalPrivateKeys(@field:ColumnInfo val regId: Int,
                             @field:ColumnInfo val idKeyPair: String,
                             @field:ColumnInfo val preKeyIds: String,
                             @field:ColumnInfo val preKeyRecord: String,
                             @field:ColumnInfo val userId: String)
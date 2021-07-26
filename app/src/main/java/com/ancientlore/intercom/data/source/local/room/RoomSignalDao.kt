package com.ancientlore.intercom.data.source.local.room

import androidx.room.*
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.crypto.SignalPrivateKeys

@Dao
interface RoomSignalDao {

	@Query("SELECT * FROM signalKeys WHERE userId = :userId")
	fun getById(userId: String): SignalPrivateKeys?

	@Insert(onConflict = OnConflictStrategy.ABORT)
	fun insert(keys: SignalPrivateKeys)
}

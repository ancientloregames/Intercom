package com.ancientlore.intercom.data.source.local.room

import androidx.room.*
import com.ancientlore.intercom.data.model.Chat

@Dao
interface RoomChatDao {

	@Query("SELECT * FROM chats WHERE userId = :userId ORDER BY id DESC")
	fun getAll(userId: String): List<Chat>

	@Query("SELECT * FROM chats WHERE id = :id")
	fun getById(id: String): Chat?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(chat: Chat)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(chats: List<Chat>)

	@Update
	fun update(vararg chat: Chat)

	@Query("DELETE FROM chats WHERE id = :id")
	fun deleteById(id: String)

	@Query("DELETE FROM chats WHERE userId = :userId")
	fun deleteAll(userId: String)

	@Query("SELECT * FROM chats WHERE id IN (:ids)")
	fun getAllByIds(ids: LongArray): List<Chat>

	@Query("UPDATE chats SET name = :name WHERE id = :id")
	fun updateName(id: String, name: String)

	@Query("UPDATE chats SET iconUrl = :url WHERE id = :id")
	fun updateIconUrl(id: String, url: String)
}

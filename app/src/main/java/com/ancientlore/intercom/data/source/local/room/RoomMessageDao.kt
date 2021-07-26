package com.ancientlore.intercom.data.source.local.room

import androidx.room.*
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message

@Dao
interface RoomMessageDao {

	@Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY id DESC")
	fun getAll(chatId: String): List<Message>

	@Query("SELECT * FROM messages WHERE chatId = :chatId AND id = :msgId")
	fun getById(msgId: String, chatId: String): Message?

	@Query("SELECT * FROM messages WHERE id IN (:ids)")
	fun getAllByIds(ids: Array<String>): List<Message>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(message: Message)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(messages: List<Message>)

	@Update
	fun update(vararg message: Message)

	@Delete
	fun delete(message: Message)

	@Query("DELETE FROM messages WHERE chatId = :chatId AND id = :msgId")
	fun deleteById(msgId: String, chatId: String)

	@Query("DELETE FROM messages WHERE chatId = :chatId")
	fun deleteAll(chatId: String)

	@Query("UPDATE messages SET attachUrl = :url WHERE chatId = :chatId AND id = :id")
	fun updateMessageUri(id: String, url: String, chatId: String)

	@Query("UPDATE messages SET status = 2 WHERE chatId = :chatId AND id = :id")
	fun setStatusReceived(id: String, chatId: String)
}
package com.ancientlore.intercom.data.source.local.room

import androidx.room.*
import com.ancientlore.intercom.data.model.User

@Dao
interface RoomUserDao {

	@Query("SELECT * FROM users ORDER BY phone DESC")
	fun getAll(): List<User>

	@Query("SELECT * FROM users WHERE phone LIKE :id")
	fun getById(id: String): User?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(user: User)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(users: List<User>)

	@Update
	fun update(vararg user: User)

	@Delete
	fun delete(user: User)

	@Query("DELETE FROM users WHERE phone = :id")
	fun deleteById(id: String)

	@Query("DELETE FROM users")
	fun deleteAll()

	@Query("UPDATE users SET token = :newToken WHERE phone = :userId")
	fun updateNotificationToken(userId: String, newToken: String)

	@Query("UPDATE users SET iconUrl = :newUrl WHERE phone = :userId")
	fun updateIconUrl(userId: String, newUrl: String)

	@Query("UPDATE users SET name = :newName WHERE phone = :userId")
	fun updateName(userId: String, newName: String)

	@Query("UPDATE users SET status = :newStatus WHERE phone = :userId")
	fun updateStatus(userId: String, newStatus: String)

	@Query("UPDATE users SET online = :online WHERE phone = :userId")
	fun updateOnlineStatus(userId: String, online: Boolean)
}
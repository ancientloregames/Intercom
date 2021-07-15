package com.ancientlore.intercom.data.source.local.room

import androidx.room.*
import com.ancientlore.intercom.data.model.Contact

@Dao
interface RoomContactDao {

	@Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY phone DESC")
	fun getAll(userId: String): List<Contact>

	@Query("SELECT * FROM contacts WHERE userId = :userId AND phone = :id")
	fun getById(id: String, userId: String): Contact?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(contact: Contact)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(contacts: List<Contact>)

	@Update
	fun update(vararg contact: Contact)

	@Delete
	fun delete(contact: Contact)

	@Query("DELETE FROM contacts WHERE userId = :userId AND phone = :id")
	fun deleteById(id: String, userId: String)

	@Query("DELETE FROM contacts")
	fun deleteAll()

	@Query("UPDATE contacts SET name = :name WHERE phone = :id")
	fun updateName(id: String, name: String)

	@Query("UPDATE contacts SET iconUrl = :url WHERE phone = :id")
	fun updateIconUrl(id: String, url: String)
}
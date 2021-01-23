package com.ancientlore.intercom.manager

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.utils.extensions.isPermissionGranted
import com.ancientlore.intercom.utils.extensions.safeClose
import java.util.*

object DeviceContactsManager {

	interface UpdateListener {
		fun onContactListUpdate(contacts: List<Item>)
	}

	data class Item(
		val id: String,
		val name: String,
		val photoUri: String,
		val numbers: List<String>
	) : Comparable<Item> {
		override fun compareTo(other: Item) = id.compareTo(other.id)

		val mainNumber: String
		 get() = numbers[0]
	}

	class PhoneContactsObserver(val context: Context) : ContentObserver(null) {
		override fun onChange(selfChange: Boolean) {
			super.onChange(selfChange)

			refreshRunnable?.let { refreshHandler.removeCallbacks(it) }

			refreshRunnable = Runnable {
				refreshRunnable = null
				updateCache(context)
				notifyUpdateListeners()
			}

			refreshHandler.postDelayed(refreshRunnable, 2000)
		}
	}

	private val contactListCache = arrayListOf<Item>()
	private var contactObserver: PhoneContactsObserver? = null
	private val CONTENT_URI = ContactsContract.Contacts.CONTENT_URI

	private val listeners = Collections.synchronizedList(LinkedList<UpdateListener>())
	private var refreshRunnable: Runnable? = null
	private val refreshHandler = Handler(Looper.getMainLooper())

	private val contactsProjection = arrayOf(
		ContactsContract.Contacts._ID,
		ContactsContract.Contacts.DISPLAY_NAME,
		ContactsContract.Contacts.PHOTO_URI,
		ContactsContract.Contacts.HAS_PHONE_NUMBER
	)

	fun enableObserver(context: Context) {
		if (contactObserver == null) {
			contactObserver = PhoneContactsObserver(context)
			context.contentResolver.registerContentObserver(CONTENT_URI, true, contactObserver)

			if (contactListCache.isEmpty())
				updateCache(context)
		}
	}

	fun disableObserver(context: Context) {
		if (contactObserver != null) {
			context.contentResolver.unregisterContentObserver(contactObserver ?: return)
			contactObserver = null
		}
	}

	fun registerUpdateListener(listener: UpdateListener) = listeners.add(listener)

	fun unregisterUpdateListener(listener: UpdateListener) = listeners.remove(listener)

	fun getContactsCache(): List<Item> = contactListCache

	fun getContacts(context: Context): List<Item> {
		if (contactListCache.isEmpty()) {
			updateCache(context)
		}

		return contactListCache
	}

	private fun updateCache(context: Context) {
		if (context.isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
			getDeviceContacts(context).let {
				contactListCache.clear()
				contactListCache.addAll(it)
			}
		}
		else
			Log.d("DeviceContactsManager", "Read Contacts permission was not granted!")
	}

	private fun notifyUpdateListeners() {
		synchronized(listeners) {
			listeners.forEach { listener ->
				Utils.runOnUiThread {
					listener.onContactListUpdate(contactListCache)
				}
			}
		}
	}

	private fun getDeviceContacts(context: Context) : List<Item> {

		val cursor = context.contentResolver.query(CONTENT_URI,
			contactsProjection, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC")
			?: return emptyList()

		val contacts = arrayListOf<Item>()

		try {
			val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
			val nameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
			val photoColumn = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
			val hasPhoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

			while (cursor.moveToNext()) {

				if (cursor.getInt(hasPhoneColumn) > 0) { // is contact has Phones

					val contactId = cursor.getString(idColumn)
					val contactPhones = context.contentResolver.getContactPhones(contactId)

					if (contactPhones.isNotEmpty()) {

						contacts.add(Item(contactId,
							cursor.getString(nameColumn),
							cursor.getString(photoColumn) ?: "",
							contactPhones
						))
					}
				}
			}
		} finally {
			cursor.safeClose()
		}

		return contacts
	}

	private fun ContentResolver.getContactPhones(contactId: String): List<String> {
		val cursor = query(
			ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
			arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
			ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
			arrayOf(contactId), null)
			?: return emptyList()

		val list = arrayListOf<String>()

		try {
			val numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
			while (cursor.moveToNext()) {
				val phone = cursor.getString(numberColumn)
				list.add(phone)
			}
		} finally {
			cursor.safeClose()
		}

		return list
	}
}
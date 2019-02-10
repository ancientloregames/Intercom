package com.ancientlore.intercom.utils

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.ancientlore.intercom.data.model.Contact
import java.io.Closeable
import java.io.IOException

fun Context.checkPermission(permission: String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

@RequiresPermission(Manifest.permission.READ_CONTACTS)
fun ContentResolver.getContacts(): List<Contact> {
	val list = mutableListOf<Contact>()

	val cursor = query(ContactsContract.Contacts.CONTENT_URI,
		null, null, null, null)

	if (cursor != null) {
		try {
			while (cursor.moveToNext()) {
				val hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
				if (hasPhone) {
					val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
					val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""
					val phone = getContactPhones(id)[0] // TODO normalize phone number
					list.add(Contact(name = name, phone = phone))
				}
			}
		} finally {
			cursor.safeClose()
		}
	}

	return list
}

@RequiresPermission(Manifest.permission.READ_CONTACTS)
fun ContentResolver.getContactPhones(contactId: String): List<String> {
	val list = mutableListOf<String>()

	val cursor = query(
		ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
		ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
		arrayOf(contactId), null)

	if (cursor != null) {
		try {
			while (cursor.moveToNext()) {
				val phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
				list.add(phone)
			}
		} finally {
			cursor.safeClose()
		}
	}

	return list
}

fun Closeable.safeClose() {
	try {
		close()
	} catch (ignore: IOException) { }
}
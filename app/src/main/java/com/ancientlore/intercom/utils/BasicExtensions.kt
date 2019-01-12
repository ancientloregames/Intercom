package com.ancientlore.intercom.utils

import android.content.ContentResolver
import android.provider.ContactsContract
import java.io.Closeable
import java.io.IOException
import java.util.ArrayList

fun ContentResolver.getContactPhones(contactId: String): List<String> {
	val cursor = query(
		ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
		ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
		arrayOf(contactId), null)

	val list = ArrayList<String>()

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
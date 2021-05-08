package com.ancientlore.intercom.utils.extensions

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.ancientlore.intercom.C
import com.ancientlore.intercom.data.model.Contact
import com.ancientlore.intercom.data.model.FileData
import com.ancientlore.intercom.utils.ImageUtils
import com.ancientlore.intercom.utils.Utils
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max


fun Context.getAppCacheDir(): File {
	var state: String? = null
	try {
		state = Environment.getExternalStorageState()
	} catch (e: Exception) {
		Utils.logError(e)
	}

	var file: File? = null
	if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
		try {
			file = externalCacheDir
		} catch (e: Exception) {
			Utils.logError(e)
		}
	}

	return file ?: cacheDir ?: Environment.getDownloadCacheDirectory()
}

fun Context.checkPermission(permission: String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
fun Context.isPermissionGranted(permission: String) = checkPermission(permission)
fun Context.isNotPermissionGranted(permission: String) = isPermissionGranted(permission).not()

fun Context.openFile(uri: Uri) : Boolean {
	val openFile = Intent(Intent.ACTION_VIEW)
	openFile.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
			or Intent.FLAG_ACTIVITY_NO_HISTORY
			or Intent.FLAG_GRANT_READ_URI_PERMISSION
			or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

	openFile.setDataAndType(uri, uri.getMimeType())

	val isOpenable = openFile.resolveActivity(packageManager) != null
	if (isOpenable)
		startActivity(openFile)

	return isOpenable
}

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

fun Bundle.isNotEmpty() = !isEmpty

fun Uri.isNotEmpty() = this != Uri.EMPTY

fun Uri.getExtension(): String {
	val filename =  lastPathSegment
	val strLength = filename.lastIndexOf(".")
	return if (strLength > 0) filename.substring(strLength + 1).toLowerCase() else ""
}

fun Uri.getFileData(contentResolver: ContentResolver) : FileData {
	val projection = arrayOf(
		MediaStore.Files.FileColumns._ID,
		MediaStore.Files.FileColumns.DISPLAY_NAME,
		MediaStore.Files.FileColumns.SIZE,
		MediaStore.Files.FileColumns.MIME_TYPE)

	val cursor = contentResolver.query(this,
		projection, null, null, null)

	if (cursor != null) {
		try {
			cursor.moveToFirst()
			val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
			val name = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: lastPathSegment
			val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
			val mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE))
			val extension = getExtension()
			return FileData(id, name, this, size, mimeType, extension)
		} finally {
			cursor.safeClose()
		}
	}
	return FileData()
}

fun Uri.getMimeType(): String {
	return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension().toLowerCase()) ?: "*/*"
}

fun Uri.isImage(): Boolean {
	val mimeType = getMimeType()
	return mimeType != null && mimeType.startsWith("image")
}

fun Uri.isVideo(): Boolean {
	val mimeType = getMimeType()
	return mimeType != null && mimeType.startsWith("video")
}

fun Uri.isVisual() : Boolean {
	val mimeType = getMimeType()
	return mimeType != null && (mimeType.startsWith("image") || mimeType.startsWith("video"))
}

fun Uri.createThumbnail(context: Context) : Uri {
	if (isVisual()) {
		val maxSize = Utils.toDp(C.THUMBNAIL_SIZE)
		val thumbnail = ImageUtils.getThumbnail(path.toString(), maxSize, maxSize)

		if (thumbnail != null) {
			val file = File(context.getAppCacheDir(), lastPathSegment)
			if (file.createNewFile()) {
				var output: FileOutputStream? = null
				try {
					output = FileOutputStream(file)
					thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, output)
					return Uri.fromFile(file)
				} finally {
					output?.safeClose()
					thumbnail.recycle()
				}
			}
		}
	}

	return Uri.EMPTY
}

fun Drawable.toBitmap(): Bitmap? {
	if (this is BitmapDrawable)
		return bitmap

	var bitmap: Bitmap? = null
	val width = max(intrinsicWidth, 2)
	val height = max(intrinsicHeight, 2)
	try {
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap!!)
		setBounds(0, 0, canvas.width, canvas.height)
		draw(canvas)
	} catch (e: Throwable) {
		e.printStackTrace()
	}

	return bitmap
}

fun EditText.showKeyboard() : Boolean {
	return showKeyboard(InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.showKeyboard(flags: Int) : Boolean {
	return context
		?.let { context ->
			if (Looper.myLooper() == Looper.getMainLooper()) {
				val success = requestFocus()
						&& (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
					.showSoftInput(this, flags)
				setSelection(length())
				return success
			} else {
				post {
					requestFocus()
					(context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
						.showSoftInput(this, flags)
					setSelection(length())
				}
			}
		} ?: false
}

fun Activity.hideKeyboard() : Boolean {
	return hideKeyboard(0)
}

fun Activity.hideKeyboard(flags: Int) : Boolean {
	return hideKeyboard(currentFocus ?: return false, flags)
}

fun Context.hideKeyboard(view: View, flags: Int) : Boolean {
	return view.context
		?.let {
			if (Looper.myLooper() == Looper.getMainLooper()) {
				(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
					.hideSoftInputFromWindow(view.windowToken, flags)
			}
			else {
				view.post {
					(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
						.hideSoftInputFromWindow(view.windowToken, flags)
				}
			}
		} ?: false
}
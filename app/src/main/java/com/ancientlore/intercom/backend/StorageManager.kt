package com.ancientlore.intercom.backend

import android.net.Uri
import com.ancientlore.intercom.data.model.FileData
import java.io.File

interface StorageManager {
	fun download(url: String, outFile: File, callback: ProgressRequestCallback<Any>)
	fun upload(data: FileData, fullPath: String, callback: ProgressRequestCallback<Uri>)
	fun uploadImage(data: FileData, path: String, callback: ProgressRequestCallback<Uri>)
	fun uploadFile(data: FileData, path: String, callback: ProgressRequestCallback<Uri>)
	fun uploadAudioMessage(uri: Uri, path: String, callback: ProgressRequestCallback<Uri>)
}
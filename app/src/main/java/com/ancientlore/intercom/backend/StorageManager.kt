package com.ancientlore.intercom.backend

import android.net.Uri
import com.ancientlore.intercom.data.model.FileData

interface StorageManager {
	fun uploadImage(data: FileData, path: String, callback: RequestCallback<Uri>)
	fun uploadFile(data: FileData, path: String, callback: RequestCallback<Uri>)
}
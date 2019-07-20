package com.ancientlore.intercom.backend

import android.net.Uri
import com.ancientlore.intercom.data.model.FileData

interface StorageManager {
	fun uploadFile(file: FileData, path: String, callback: RequestCallback<Uri>)
}
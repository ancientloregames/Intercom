package com.ancientlore.intercom.backend

import android.net.Uri
import com.ancientlore.intercom.data.model.LocalFile

interface StorageManager {
	fun uploadFile(file: LocalFile, path: String, callback: RequestCallback<Uri>)
}
package com.ancientlore.intercom.backend.firebase

import android.net.Uri
import com.ancientlore.intercom.backend.ProgressRequestCallback
import com.ancientlore.intercom.backend.StorageManager
import com.ancientlore.intercom.data.model.FileData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

object FirebaseStorageManager : StorageManager {

	private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

	override fun uploadImage(data: FileData, path: String, callback: ProgressRequestCallback<Uri>) {
		val fileRef = storage.getReference("images/$path/${data.name}")
		upload(data.uri, fileRef, callback)
	}

	override fun uploadFile(data: FileData, path: String, callback: ProgressRequestCallback<Uri>) {
		val fileRef = storage.getReference("files/$path/${data.name}")
		upload(data.uri, fileRef, callback)
	}

	override fun uploadAudioMessage(uri: Uri, path: String, callback: ProgressRequestCallback<Uri>) {
		val fileRef = storage.getReference("files/$path/${uri.lastPathSegment}")
		upload(uri, fileRef, callback)
	}

	private fun upload(uri: Uri, fileRef: StorageReference, callback: ProgressRequestCallback<Uri>) {
		fileRef.putFile(uri)
			.addOnProgressListener {
				val progress = it.bytesTransferred / it.totalByteCount.toFloat() * 100
				callback.onProgress(progress.toInt())
			}
			.continueWithTask( Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
				if (!task.isSuccessful)
					task.exception?.let {
						callback.onFailure(it)
					}

				return@Continuation fileRef.downloadUrl
			})
			.addOnSuccessListener { downloadUri -> callback.onSuccess(downloadUri) }
			.addOnFailureListener { exception -> callback.onFailure(exception) }
	}
}
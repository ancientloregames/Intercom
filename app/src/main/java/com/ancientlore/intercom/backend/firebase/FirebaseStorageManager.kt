package com.ancientlore.intercom.backend.firebase

import android.net.Uri
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.ProgressRequestCallback
import com.ancientlore.intercom.backend.StorageManager
import com.ancientlore.intercom.data.model.FileData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object FirebaseStorageManager : StorageManager {

	private val service: ExecutorService = Executors.newSingleThreadExecutor { r -> Thread(r, "fsStorageManager_thread") }

	private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

	override fun download(url: String, outFile: File, callback: ProgressRequestCallback<Any>) {
		storage.getReferenceFromUrl(url).getFile(outFile)
			.addOnProgressListener {
				val progress = it.bytesTransferred / it.totalByteCount.toFloat() * 100
				callback.onProgress(progress.toInt())
			}
			.addOnSuccessListener { callback.onSuccess(EmptyObject) }
			.addOnFailureListener { exception -> callback.onFailure(exception) }
	}

	override fun upload(data: FileData, fullPath: String, callback: ProgressRequestCallback<Uri>) {
		val fileRef = storage.getReference("$fullPath/${data.name}")
		upload(data.uri, fileRef, callback)
	}

	override fun uploadImage(data: FileData, path: String, callback: ProgressRequestCallback<Uri>) {
		val fileRef = storage.getReference("images/$path/${data.name}")
		upload(data.uri, fileRef, callback)
	}

	override fun uploadFile(data: FileData, path: String, callback: ProgressRequestCallback<Uri>) {
		val fileRef = storage.getReference("files/$path/${data.name}")
		upload(data.uri, fileRef, callback)
	}

	override fun uploadAudioMessage(uri: Uri, path: String, callback: ProgressRequestCallback<Uri>) {
		val fileRef = storage.getReference("audio/$path/${uri.lastPathSegment}")
		upload(uri, fileRef, callback)
	}

	private fun upload(uri: Uri, fileRef: StorageReference, callback: ProgressRequestCallback<Uri>) {
		fileRef.putFile(uri)
			.addOnProgressListener {
				exec {
					val progress = it.bytesTransferred / it.totalByteCount.toFloat() * 100
					callback.onProgress(progress.toInt())
				}
			}
			.continueWithTask( Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
				if (!task.isSuccessful)
					task.exception?.let {
						exec { callback.onFailure(it) }
					}

				return@Continuation fileRef.downloadUrl
			})
			.addOnSuccessListener { downloadUri -> exec { callback.onSuccess(downloadUri) } }
			.addOnFailureListener { exception -> exec { callback.onFailure(exception) } }
	}

	private fun exec(command: Runnable) {
		service.execute(command)
	}
}
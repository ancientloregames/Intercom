package com.ancientlore.intercom.service

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.ancientlore.intercom.App
import kotlin.Throws
import com.ancientlore.intercom.C.ICON_DIR_PATH
import com.ancientlore.intercom.backend.ProgressRequestCallback
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.FileData
import com.ancientlore.intercom.data.source.ChatRepository
import com.ancientlore.intercom.utils.Utils
import java.io.IOException

class ChatIconUploadService : FileUploadService() {

	companion object {
		private val TAG = ChatIconUploadService::class.java.simpleName

		const val EXTRA_CHAT_ID = "extra_chat_id"           // String
		const val EXTRA_USER_ID = "extra_user_id"           // String
		const val EXTRA_CHAT_PARTICIPANTS = "extra_chat_part"  // ArrayList<String>
	}

	private inner class Params(intent: Intent): FileUploadService.Params(intent) {

		val chatId: String? = intent.getStringExtra(EXTRA_CHAT_ID)
		val userId: String? = intent.getStringExtra(EXTRA_USER_ID)
		val participants: List<String>? = intent.getStringArrayListExtra(EXTRA_CHAT_PARTICIPANTS)

		val iconUri: Uri get() = uriList[0]

		public override fun isValid(): Boolean {
			return super.isValid()
					&& uriList.size == 1
					&& chatId != null
					&& userId != null
					&& participants != null
					&& participants.isNotEmpty()
		}
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		Log.d(TAG, "onStartCommand:$intent:$startId")

		if (ACTION_UPLOAD == intent.action) {

			val params = Params(intent)
			if (params.isValid) {

				try {
					taskStarted()
					val file = compress(params.iconUri)
					file?.let { upload(it, params) }
				} catch (e: IOException) {
					e.printStackTrace()
				}
			}
			else taskCompleted()
		}
		else taskCompleted()

		return START_REDELIVER_INTENT
	}

	@Throws(IOException::class)
	private fun upload(fileData: FileData, params: Params) {
		Log.d(TAG, "upload: ${fileData.name}")

		App.backend.getStorageManager()
			.uploadImage(fileData, ICON_DIR_PATH, object : ProgressRequestCallback<Uri> {

				override fun onProgress(progress: Int) {

					if (params.showNotice)
						showUploadProgressNotification(progress)
				}
				override fun onSuccess(result: Uri) {
					Log.d(TAG, "upload: onSuccess upload to storage")

					updateRepository(result, params)
				}
				override fun onFailure(error: Throwable) {
					Utils.logError("ChatIconUploadService.upload.storage.onFailure. uri: ${fileData.uri}", error)

					handleUploadFinish(null, params.iconUri, params)

					taskCompleted()
				}
			})
	}

	private fun updateRepository(serverUri: Uri, params: Params) {

		ChatRepository.apply {
			setRemoteSource(App.backend.getDataSourceProvider().getChatSource(params.userId!!) )
		}
			.updateItem(Chat(
				id = params.chatId!!,
				iconUrl = serverUri.toString(),
				participants = params.participants!!), object : RequestCallback<Any> {

				override fun onSuccess(result: Any) {
					Log.d(TAG, "upload: onSuccess update in database")

					handleUploadFinish(serverUri, params.iconUri, params)

					taskCompleted()
				}
				override fun onFailure(error: Throwable) {
					Utils.logError("ChatIconUploadService.upload.db.onFailure. uri: $serverUri", error)

					handleUploadFinish(serverUri, params.iconUri, params)

					taskCompleted()
				}
			})
	}
}
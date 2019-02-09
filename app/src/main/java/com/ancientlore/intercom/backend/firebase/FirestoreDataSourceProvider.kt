package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.data.DataSourceProvider
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreChatSource
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreContactSource
import com.ancientlore.intercom.data.source.remote.firestore.FirestoreMessageSource
import com.google.firebase.auth.FirebaseAuth
import java.lang.RuntimeException

object FirestoreDataSourceProvider
	: DataSourceProvider {

	private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid
		?: throw RuntimeException("Call data source provider only after authorization!") }

	override fun getChatSource() = FirestoreChatSource.getInstance(userId)

	override fun getMessageSource() = FirestoreMessageSource.getInstance(userId)

	override fun getContactSource() = FirestoreContactSource.getInstance(userId)
}
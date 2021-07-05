package com.ancientlore.intercom.data.source.remote.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class FirestoreSource<T> {

	private val service: ExecutorService = Executors.newSingleThreadExecutor { r -> Thread(r, getWorkerThreadName()) }

	protected val db = FirebaseFirestore.getInstance()

	protected abstract fun getObjectClass(): Class<T>

	protected abstract fun getWorkerThreadName(): String

	protected fun deserialize(snapshot: DocumentSnapshot): T? = snapshot.toObject(getObjectClass())

	protected fun deserialize(snapshot: QuerySnapshot): List<T> = snapshot.toObjects(getObjectClass())

	protected fun exec(command: Runnable) {
		service.execute(command)
	}
}
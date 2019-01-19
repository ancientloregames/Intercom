package com.ancientlore.intercom.data.source.remote.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

abstract class FirestoreSource<T> {

	protected val db = FirebaseFirestore.getInstance()

	protected abstract fun getObjectClass(): Class<T>

	protected fun deserialize(snapshot: DocumentSnapshot): T? = snapshot.toObject(getObjectClass())

	protected fun deserialize(snapshot: QuerySnapshot): List<T> = snapshot.toObjects(getObjectClass())
}
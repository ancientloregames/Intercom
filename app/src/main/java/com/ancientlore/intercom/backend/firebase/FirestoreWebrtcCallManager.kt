package com.ancientlore.intercom.backend.firebase

import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.WebrtcCallManager
import com.ancientlore.intercom.data.model.call.Answer
import com.ancientlore.intercom.data.model.call.Candidate
import com.ancientlore.intercom.data.model.call.Offer
import com.ancientlore.intercom.utils.Utils
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.webrtc.Logging
import org.webrtc.SessionDescription
import java.lang.RuntimeException

object FirestoreWebrtcCallManager : WebrtcCallManager() {

	private const val CALLS = "calls"
	private const val OFFERS = "offers"
	private const val ANSWERS = "aswers"
	private const val CANDIDATES = "candidates"

	private val calls get() = FirebaseFirestore.getInstance().collection(CALLS)

	override fun sendOffer(targetId: String, offer: HashMap<*, *>) {
		calls
			.document(targetId)
			.collection(OFFERS)
			.document(userId)
			.set(offer, SetOptions.merge())
			.addOnSuccessListener { Logging.d(TAG, "sendOffer: success") }
			.addOnFailureListener {
				Logging.e(TAG, "sendOffer: failure")
				Utils.logError(it, TAG)
			}
	}

	override fun sendAnswer(targetId: String, answer: HashMap<*, *>) {
		calls
			.document(targetId)
			.collection(ANSWERS)
			.document(userId)
			.set(answer, SetOptions.merge())
			.addOnSuccessListener { Logging.d(TAG, "sendAnswer: success") }
			.addOnFailureListener {
				Logging.e(TAG, "sendAnswer: failure")
				Utils.logError(it, TAG)
			}
	}

	override fun sendCandidate(targetId: String, type: SessionDescription.Type, candidate: HashMap<*, *>) {
		getCandidateDocument(targetId, type)
			.set(candidate, SetOptions.merge())
			.addOnSuccessListener { Logging.d(TAG, "sendCandidate: success") }
			.addOnFailureListener {
				Logging.e(TAG, "sendCandidate: failure")
				Utils.logError(it, TAG)
			}
	}

	override fun attachOfferListener(callback: RequestCallback<Offer>): RepositorySubscription {
		val sub = calls
			.document(userId)
			.collection(OFFERS)
			.addSnapshotListener { snapshot, error ->
				if (error != null) {
					callback.onFailure(error)
					return@addSnapshotListener
				}
				else if (snapshot != null) {
					for (dc in snapshot.documentChanges) {
						if (!dc.document.metadata.isFromCache && !dc.document.metadata.hasPendingWrites()) {
							when (dc.type) {
								DocumentChange.Type.ADDED,
								DocumentChange.Type.MODIFIED -> {
									Logging.d(TAG, "Firestore.onNewOffer")
									val callerId = dc.document.data[FIELD_CALLER_ID]
									val sdp = dc.document.data[FIELD_SDP]
									if (callerId is String && sdp is String) {
										callback.onSuccess(Offer(callerId, sdp))
									}
									else
										callback.onFailure(RuntimeException("Wrong offer params"))
								}
								else -> {}
							}
						}
					}
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				sub.remove()
			}
		}
	}

	override fun attachAnswerListener(targetId: String, callback: RequestCallback<Answer>): RepositorySubscription {
		val sub = calls
			.document(userId)
			.collection(ANSWERS)
			.document(targetId)
			.addSnapshotListener { snapshot, error ->
				if (error != null) {
					callback.onFailure(error)
					return@addSnapshotListener
				}
				else if (snapshot != null) {
					if (!snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
						Logging.d(TAG, "Firestore.onNewAnswer")
						val sdp = snapshot.get(FIELD_SDP)
						if (sdp is String) {
							callback.onSuccess(Answer(sdp))
						}
						else
							callback.onFailure(RuntimeException("Wrong answer params"))
					}
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				sub.remove()
			}
		}
	}

	override fun attachCandidateListener(targetId: String, type: SessionDescription.Type,
		callback: RequestCallback<Candidate>): RepositorySubscription {

		val sub = getCandidateDocument(targetId, type)
			.addSnapshotListener { snapshot, error ->
				if (error != null) {
					callback.onFailure(error)
					return@addSnapshotListener
				}
				else if (snapshot != null) {
					if (!snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
						Logging.d(TAG, "Firestore.onNewCandidate")
						val id = snapshot.get(FIELD_ID)
						val label = snapshot.get(FIELD_LABEL)
						val candidate = snapshot.get(FIELD_CANDIDATE)
						if (id is String && label is Number && candidate is String) {
							callback.onSuccess(Candidate(id, label.toInt(), candidate))
						}
						else
							callback.onFailure(RuntimeException("Wrong candidate params"))
					}
				}
			}

		return object : RepositorySubscription {
			override fun remove() {
				sub.remove()
			}
		}
	}

	private fun getCandidateDocument(targetId: String, type: SessionDescription.Type) : DocumentReference {
		return when (type) {
			SessionDescription.Type.ANSWER -> calls.document(targetId)
				.collection(CANDIDATES)
				.document(userId)
			SessionDescription.Type.OFFER -> calls.document(userId)
				.collection(CANDIDATES)
				.document(targetId)
			else -> {
				Utils.logError(TAG, "Unknown SessionDescription Type: ${type.name}")
				throw RuntimeException()
			}
		}
	}
}
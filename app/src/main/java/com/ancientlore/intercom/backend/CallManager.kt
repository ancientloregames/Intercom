package com.ancientlore.intercom.backend

import com.ancientlore.intercom.data.model.call.Offer
import com.ancientlore.intercom.utils.Runnable1

interface CallManager<T> {

	data class CallParams<T>(val targetId: String,
	                         val localVideoView: T,
	                         val remoteVideoView: T)

	data class AudioCallParams(val targetId: String)

	fun call(params: CallParams<T>)

	fun call(params: AudioCallParams)

	fun answer(params: CallParams<T>, sdp: String) //FIXME second arg looks reductant. merge with params?

	fun answer(params: AudioCallParams, sdp: String)

	fun hungup() : Boolean

	fun setIncomingCallHandler(callback: Runnable1<Offer>)

	fun setCallConnectionListener(listener: CallConnectionListener?)
}
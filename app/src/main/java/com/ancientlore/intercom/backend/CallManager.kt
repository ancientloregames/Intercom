package com.ancientlore.intercom.backend

import com.ancientlore.intercom.data.model.call.Offer
import com.ancientlore.intercom.utils.Runnable1

interface CallManager<T> {

	data class CallParams<T>(val targetId: String,
	                         val localVideoView: T,
	                         val remoteVideoView: T)

	fun call(params: CallParams<T>)

	fun answer(params: CallParams<T>, sdp: String) //FIXME second arg looks reductant. merge with params?

	fun hungup() : Boolean

	fun setIncomingCallHandler(callback: Runnable1<Offer>)

	fun setCallConnectionListener(listener: CallConnectionListener?)
}
package com.ancientlore.intercom.data.model.call

import androidx.annotation.IntDef

data class Offer(val callerId: String = "",
                 @CallType val callType: Int = CALL_TYPE_AUDIO,
                 val sdp: String = "") {

  companion object {
    const val CALL_TYPE_AUDIO = 0
    const val CALL_TYPE_VIDEO = 1
  }

  @IntDef(CALL_TYPE_AUDIO, CALL_TYPE_VIDEO)
  @Retention(AnnotationRetention.SOURCE)
  annotation class CallType
}

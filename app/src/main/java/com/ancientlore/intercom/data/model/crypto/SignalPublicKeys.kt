package com.ancientlore.intercom.data.model.crypto

data class SignalPublicKeys(val regId: Int,
                            val idKeyPair: String,
                            val preKeys: List<String>,
                            val signedId: Int,
                            val signedSignature: String,
                            val signedPublicKey: String)
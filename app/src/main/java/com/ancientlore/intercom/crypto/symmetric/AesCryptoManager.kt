package com.ancientlore.intercom.crypto.symmetric

object AesCryptoManager: SymmetricCryptoManager() {

	override fun getTransformation() = "AES" // Present in Android since API 1

	override fun getKey(): ByteArray { // FIXME need some obfuscation. Maybe put it in C++ code

		return byteArrayOf(27, -12, 51, -44, 105, 4, -31, -34, 68, 51, 17, -20, 9, -105, 40, 67)
	}
}
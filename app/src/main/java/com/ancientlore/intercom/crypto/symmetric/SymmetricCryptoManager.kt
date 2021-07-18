package com.ancientlore.intercom.crypto.symmetric

import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.crypto.CryptoException
import com.ancientlore.intercom.crypto.CryptoManager
import com.ancientlore.intercom.crypto.CryptoUtils
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.utils.Utils
import java.lang.Exception
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

abstract class SymmetricCryptoManager: CryptoManager {

	private var cipher: Cipher? = null
	private var decipher: Cipher? = null

	/**
	 * @see javax.crypto.Cipher header
	 */
	protected abstract fun getTransformation(): String

	protected abstract fun getKey(): ByteArray

	init {

		val transformation = getTransformation()

		val secretKeySpec = SecretKeySpec(getKey(), transformation)

		try {
			cipher = Cipher.getInstance(transformation).apply {
				init(Cipher.ENCRYPT_MODE, secretKeySpec)
			}
			decipher = Cipher.getInstance(transformation).apply {
				init(Cipher.DECRYPT_MODE, secretKeySpec)
			}
		} catch (e: NoSuchAlgorithmException) {
			Utils.logError(e)
		} catch (e: NoSuchPaddingException) { // No padding assigned
			Utils.logError(e)
		}
	}

	private fun encrypt(string: String): String {

		var encrypted: String = string
		try {
			val encryptedByte = cipher!!.doFinal(CryptoUtils.toBytes(string))
			encrypted = CryptoUtils.encodeToString(encryptedByte)
		} catch (e: Exception) {
			/*  InvalidKeyException, BadPaddingException,
			    IllegalBlockSizeException, UnsupportedEncodingException */
			Utils.logError(e)
		}
		return encrypted
	}

	private fun decrypt(string: String): String {

		var decrypted: String = string
		try {
			val encryptedByte = CryptoUtils.decode(string)
			val decryption = decipher!!.doFinal(encryptedByte)
			decrypted = CryptoUtils.toString(decryption)
		} catch (e: Exception) {
			/*  InvalidKeyException, BadPaddingException,
			    IllegalBlockSizeException, UnsupportedEncodingException */
			Utils.logError(e)
		}
		return decrypted
	}

	override fun encrypt(message: Message, callback: RequestCallback<Any>) {

		cipher
			?.let {
				message.text = encrypt(message.text)
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(CryptoException)

	}

	override fun decryptMessages(messages: List<Message>, callback: RequestCallback<Any>) {

		decipher
			?.let {
				for (message in messages) {
					message.text = decrypt(message.text)
				}
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(CryptoException)
	}

	override fun decryptChats(chats: List<Chat>, callback: RequestCallback<Any>) {

		decipher
			?.let {
				for (chat in chats) {
					chat.lastMsgText = decrypt(chat.lastMsgText)
				}
				callback.onSuccess(EmptyObject)
			}
			?: callback.onFailure(CryptoException)
	}
}
package com.ancientlore.intercom.crypto.signal

import com.ancientlore.intercom.App
import com.ancientlore.intercom.EmptyObject
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.backend.SimpleRequestCallback
import com.ancientlore.intercom.crypto.CryptoManager
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.model.crypto.SignalPrivateKeys
import com.ancientlore.intercom.data.model.crypto.SignalPublicKeys
import com.ancientlore.intercom.utils.Logger
import com.ancientlore.intercom.utils.Utils
import java.util.*

class SignalCryptoManager(private val userId: String): CryptoManager {

	companion object {

		private val logger: Logger = Logger("Signal")
	}

	private lateinit var localUser: SignalLocalUser

	private var signalSession: SignalSession? = null

	init {
		logger.d("init")

		val keychainSource = App.frontend.getDataSourceProvider()
			.getSignalKeychainSource(userId)
		keychainSource.getKeychain(userId, object : RequestCallback<SignalPrivateKeys> {

				override fun onSuccess(result: SignalPrivateKeys) {
					logger.d("Acquiring the private keychain: Success")
					localUser = SignalLocalUser(userId, result)
				}
				override fun onFailure(error: Throwable) {
					logger.d("Acquiring the private keychain: Failure")

					val keychain = SignalKeychain.generate(userId)
					val privateKeys = keychain.privateKeys

					localUser = SignalLocalUser(userId, privateKeys)

					logger.d("The keychain is generated")

					keychainSource.putKeychain(privateKeys, object : RequestCallback<Any> {
						override fun onSuccess(result: Any) {
							logger.d("Putting the keychain to the local data source: Success")
						}
						override fun onFailure(error: Throwable) {
							logger.d("Putting the keychain to the local data source: Failure")
						}
					})
					App.backend.getDataSourceProvider().getSignalKeychainSource(userId)
						.putKeychain(keychain.publicKeys, object : RequestCallback<Any> {
							override fun onSuccess(result: Any) {
								logger.d("Putting the keychain to the remote data source: Success")
							}
							override fun onFailure(error: Throwable) {
								logger.d("Putting the keychain to the remote data source: Failure")
							}
						})
				}
			})
	}

	override fun decryptChats(chats: List<Chat>, callback: RequestCallback<Any>) {
		logger.d("Decrypting chats: ${chats.size}")

		if (chats.isEmpty()) {
			callback.onSuccess(EmptyObject)
			return
		}

		val userChats = LinkedList<Chat>()
		val remoteChats = LinkedList<Chat>()
		for (chat in chats) {
			if (chat.lastMsgSenderId == userId)
				userChats.add(chat)
			else
				remoteChats.add(chat)
		}

		App.frontend.getDataSourceProvider()
			.getChatSource(userId)
			.getItems(userChats.map { it.id }, object : RequestCallback<List<Chat>> {

				override fun onSuccess(result: List<Chat>) {
					logger.d("Loading the user chats: Success ${userChats.size}")

					val decryptedChats = LinkedList(result)
					for (message in userChats) {
						val iter = decryptedChats.iterator()
						while (iter.hasNext()) {
							val decryptedMessage = iter.next()
							if (decryptedMessage.id == message.id) {
								message.lastMsgText = decryptedMessage.lastMsgText
								iter.remove()
								break
							}
						}
					}

					if (remoteChats.isNotEmpty()) {
						decryptRemoteChats(remoteChats, object : SimpleRequestCallback<Any>() {

							override fun onSuccess(result: Any) {
								logger.d("Decrypting the remote chats: Success")
								callback.onSuccess(EmptyObject)
							}
						})
					}
					else { // Only user chats in list
						logger.d("No remote chats to decrypt")
						callback.onSuccess(EmptyObject)
					}
				}
				override fun onFailure(error: Throwable) {
					logger.d("Decrypting the remote chats: Success")
					Utils.logError("SignalCryptoManager. Failed to decrypt local user chats")
					callback.onSuccess(EmptyObject)
				}
			})
	}

	override fun encrypt(message: Message, callback: RequestCallback<Any>) {
		logger.d("Encrypting a message")

		if (message.receivers.isEmpty()) {
			callback.onSuccess(EmptyObject)
			return
		}

		val remoteUserId = message.receivers.first { it != userId }

		if (signalSession == null || signalSession!!.remoteUserName != remoteUserId) {
			logger.d("Creating a session")

			App.backend.getDataSourceProvider()
				.getSignalKeychainSource(remoteUserId)
				.getKeychain(remoteUserId, object : RequestCallback<SignalPublicKeys> {

					override fun onSuccess(result: SignalPublicKeys) {
						logger.d("Acquiring the remote public keychain: Success")

						signalSession = SignalSession(
							localUser,
							SignalRemoteUser(remoteUserId, result))

						message.apply {
							message.text = signalSession!!.encrypt(message.text)
						}
						callback.onSuccess(EmptyObject)
					}
					override fun onFailure(error: Throwable) {
						logger.d("Acquiring the remote public keychain: Failure")
						Utils.logError(error)
						callback.onSuccess(EmptyObject) // Remote messages not decrypted
					}
				})
		}
		else { // signal session exists and consistent
			logger.d("Present session is consistent")
			message.apply {
				message.text = signalSession!!.encrypt(message.text)
			}
			callback.onSuccess(EmptyObject)
		}
	}

	override fun decryptMessages(messages: List<Message>, callback: RequestCallback<Any>) {
		logger.d("Decrypting messages: ${messages.size}")

		if (messages.isEmpty()) {
			callback.onSuccess(EmptyObject)
			return
		}

		val userMessages = LinkedList<Message>()
		val remoteMessages = LinkedList<Message>()
		for (message in messages) {
			if (message.senderId == userId)
				userMessages.add(message)
			else
				remoteMessages.add(message)
		}

		val chatId = messages[0].chatId // Assume all are from one chat otherwise something went horribly wrong

		App.frontend.getDataSourceProvider()
			.getMessageSource(chatId)
			.getItems(userMessages.map { it.id }, object : RequestCallback<List<Message>> {

				override fun onSuccess(result: List<Message>) {
					logger.d("Loading the user messages: Success ${userMessages.size}")

					val decryptedMessages = LinkedList(result)
					for (message in userMessages) {
						val iter = decryptedMessages.iterator()
						while (iter.hasNext()) {
							val decryptedMessage = iter.next()
							if (decryptedMessage.id == message.id) {
								message.text = decryptedMessage.text
								iter.remove()
								break
							}
						}
					}

					if (remoteMessages.isNotEmpty()) {
						decryptRemoteMessages(remoteMessages, object : SimpleRequestCallback<Any>() {

							override fun onSuccess(result: Any) {
								logger.d("Decrypting the remote messages: Success")
								callback.onSuccess(EmptyObject)
							}
						})
					}
					else { // Only user messages in list
						logger.d("No remote messages to decrypt")
						callback.onSuccess(EmptyObject)
					}
				}
				override fun onFailure(error: Throwable) {
					logger.d("Decrypting the remote messages: Success")
					Utils.logError("SignalCryptoManager. Failed to decrypt local user message")
					callback.onSuccess(EmptyObject)
				}
			})
	}

	private fun decryptRemoteMessages(messages: List<Message>, callback: RequestCallback<Any>) {
		logger.d("Decrypt the remote messages ${messages.size}")

		val remoteUserId = messages[0].senderId // Only individual chats supported

		if (signalSession == null || signalSession!!.remoteUserName != remoteUserId) {
			logger.d("Creating a session")

			App.backend.getDataSourceProvider()
				.getSignalKeychainSource(remoteUserId)
				.getKeychain(remoteUserId, object : RequestCallback<SignalPublicKeys> {

					override fun onSuccess(result: SignalPublicKeys) {
						logger.d("Acquiring the remote public keychain: Success")

						signalSession = SignalSession(
							localUser,
							SignalRemoteUser(remoteUserId, result))

						for (message in messages) {
							message.apply {
								message.text = signalSession!!.decrypt(message.text)
							}
						}
						callback.onSuccess(EmptyObject)
					}
					override fun onFailure(error: Throwable) {
						logger.d("Acquiring the remote public keychain: Failure")
						Utils.logError(error)
						callback.onSuccess(EmptyObject) // Remote messages not decrypted
					}
				})
		}
		else { // signal session exists and consistent
			logger.d("Present session is consistent")
			for (message in messages) {
				message.apply {
					message.text = signalSession!!.decrypt(message.text)
				}
			}
			callback.onSuccess(EmptyObject)
		}
	}

	private fun decryptRemoteChats(chats: List<Chat>, callback: RequestCallback<Any>) {
		logger.d("Decrypt the remote chats ${chats.size}")

		val remoteUserId = chats[0].lastMsgSenderId // Only individual chats supported

		if (signalSession == null || signalSession!!.remoteUserName != remoteUserId) {
			logger.d("Creating a session")

			App.backend.getDataSourceProvider()
				.getSignalKeychainSource(remoteUserId)
				.getKeychain(remoteUserId, object : RequestCallback<SignalPublicKeys> {

					override fun onSuccess(result: SignalPublicKeys) {
						logger.d("Acquiring the remote public keychain: Success")

						signalSession = SignalSession(
							localUser,
							SignalRemoteUser(remoteUserId, result))

						for (chat in chats) {
							chat.apply {
								chat.lastMsgText = signalSession!!.decrypt(chat.lastMsgText)
							}
						}
						callback.onSuccess(EmptyObject)
					}
					override fun onFailure(error: Throwable) {
						logger.d("Acquiring the remote public keychain: Failure")
						Utils.logError(error)
						callback.onSuccess(EmptyObject) // Remote chats not decrypted
					}
				})
		}
		else { // signal session exists and consistent
			logger.d("Present session is consistent")
			for (chat in chats) {
				chat.apply {
					chat.lastMsgText = signalSession!!.decrypt(chat.lastMsgText)
				}
			}
			callback.onSuccess(EmptyObject)
		}
	}
}
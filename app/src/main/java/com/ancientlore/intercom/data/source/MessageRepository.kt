package com.ancientlore.intercom.data.source

import android.net.Uri
import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Message
import com.ancientlore.intercom.data.source.cache.CacheMessageSource
import com.ancientlore.intercom.data.source.dummy.DummyMessageSource
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class MessageRepository : MessageSource {

	private var remoteSource: MessageSource = DummyMessageSource
	private var localSource: MessageSource? = null
	private val cacheSource = CacheMessageSource

	private var paginationLimit = C.DEF_MSG_PAGINATION_LIMIT

	private val userId = App.backend.getAuthManager().getCurrentUser().id

	override fun getSourceId() = remoteSource.getSourceId()

	override fun getAll(): Single<List<Message>> {

		return remoteSource.getAll()
			.flatMap { all ->
				App.frontend.getCryptoManager(userId)
					.decryptMessages(all)
					.flatMap {
						cacheSource.reset(it)
						localSource?.addItems(it)
						Single.just(it)
					}
			}
			.onErrorResumeNext {
				Utils.logError(it)
				cacheSource.getAll()
			}
			.onErrorResumeNext {
				Utils.logError(it)
				localSource?.getAll()
					?: Single.error(EmptyResultException)
			}
	}

	override fun getNextPage(): Single<List<Message>> {

		return remoteSource.getNextPage()
			.flatMap { all ->
				App.frontend.getCryptoManager(userId)
					.decryptMessages(all)
					.flatMap {
						cacheSource.reset(it)
						localSource?.addItems(it)
						Single.just(it)
					}
			}
			.onErrorResumeNext {
				Utils.logError(it)
				cacheSource.getNextPage()
			}
			.onErrorResumeNext {
				Utils.logError(it)
				localSource?.getNextPage()
					?: Single.error(EmptyResultException)
			}
	}

	override fun getAllByIds(ids: Array<String>, callback: RequestCallback<List<Message>>) {
		callback.onFailure(EmptyResultException)
	}

	override fun getItem(id: String, callback: RequestCallback<Message>) {

		remoteSource.getItem(id, object : RequestCallback<Message> {

			override fun onSuccess(result: Message) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	override fun addItem(item: Message): Single<String> {

		// Advanced encryption protocols like Signal make user messages unencryptable by user himself,
		// hence need to store them unencrypted locally
		val originalMessage = item.clone()

		return App.frontend.getCryptoManager(item.senderId).encrypt(item)
			.flatMap {
				remoteSource.addItem(item)
					.doAfterSuccess {
						originalMessage.id = it
						originalMessage.timestamp = Date(System.currentTimeMillis()) // FIXME dummy
						cacheSource.addItem(originalMessage)
						localSource?.addItem(originalMessage)
					}
			}
	}

	override fun addItem(item: Message, callback: RequestCallback<String>) {}

	override fun addItems(items: List<Message>, callback: RequestCallback<List<String>>) {

		remoteSource.addItems(items, object : RequestCallback<List<String>> {

			override fun onSuccess(result: List<String>) {
				for (i in 0..result.size) {
					items[i].id = result[i]
				}
				cacheSource.addItems(items)
				localSource?.addItems(items)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun deleteItem(id: String, callback: RequestCallback<Any>) {

		remoteSource.deleteItem(id, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.deleteItem(id)
				localSource?.deleteItem(id)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun updateMessageUri(id: String, uri: Uri): Single<Any> {
		return remoteSource.updateMessageUri(id, uri)
			.doAfterSuccess {
				cacheSource.updateMessageUri(id, uri)
				localSource?.updateMessageUri(id, uri)
			}
	}

	override fun setMessageStatusReceived(id: String, callback: RequestCallback<Any>) {

		remoteSource.setMessageStatusReceived(id, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.setMessageStatusReceived(id)
				localSource?.setMessageStatusReceived(id)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun attachChangeListener(): Observable<ListChanges<Message>> {
		//FIXME there is an issue with the decryption. Maybe .to fires without waiting for the crypto single?
		return remoteSource.attachChangeListener()
			.map { changes ->
				App.frontend.getCryptoManager(userId)
					.decryptMessages(changes.addList.plus(changes.modifyList))
					.to { changes }
			}
			.flatMap {
				cacheSource.deleteItems(it.removeList)
				//TODO localSource?.deleteItems(result.removeList)

				cacheSource.addItems(it.addList)
				localSource?.addItems(it.addList)

				cacheSource.addItems(it.modifyList)
				localSource?.addItems(it.modifyList)

				Observable.just(it)
			}
	}

	override fun attachListener(id: String, callback: RequestCallback<Message>): RepositorySubscription {

		return remoteSource.attachListener(id, object : RequestCallback<Message> {

			override fun onSuccess(result: Message) {
				cacheSource.addItem(result)
				localSource?.addItem(result)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				getItemFallback(id, callback)
			}
		})
	}

	override fun setPaginationLimit(limit: Long) {
		if (limit > 1) {
			this.paginationLimit = limit
			remoteSource.setPaginationLimit(limit)
			localSource?.setPaginationLimit(limit)
		}
		else Utils.logError("Pagination limit must be > 1")
	}

	fun setRemoteSource(source: MessageSource) {
		if (remoteSource == source)
			return

		remoteSource.clean()
		cacheSource.clear()

		remoteSource = source

		source.setPaginationLimit(paginationLimit)
		cacheSource.setPaginationLimit(paginationLimit)

		cacheSource.clear()
		localSource?.let {
			if (source.getSourceId() != it.getSourceId()) {
				it.clean()
				localSource = null
			}
		}
	}

	fun setLocalSource(source: MessageSource) {
		if (localSource == source)
			return

		localSource?.clean()

		localSource = source

		source.setPaginationLimit(paginationLimit)
		cacheSource.setPaginationLimit(paginationLimit)

		if (source.getSourceId() != remoteSource.getSourceId()) {
			cacheSource.clear()
			remoteSource.clean()
			remoteSource = DummyMessageSource
		}
	}

	private fun getItemFallback(id: String, callback: RequestCallback<Message>) {

		cacheSource.getItem(id, object : RequestCallback<Message> {

			override fun onSuccess(result: Message) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getItem(id, object : RequestCallback<Message> {

						override fun onSuccess(result: Message) {
							cacheSource.addItem(result)
							callback.onSuccess(result)
						}
						override fun onFailure(error: Throwable) {
							callback.onFailure(EmptyResultException)
						}
					}) }
			}
		})
	}
}
package com.ancientlore.intercom.data.source

import com.ancientlore.intercom.App
import com.ancientlore.intercom.backend.RepositorySubscription
import com.ancientlore.intercom.backend.RequestCallback
import com.ancientlore.intercom.data.model.Chat
import com.ancientlore.intercom.data.source.cache.CacheChatSource
import com.ancientlore.intercom.data.source.dummy.DummyChatSource
import com.ancientlore.intercom.utils.Utils
import io.reactivex.Observable
import io.reactivex.Single

object ChatRepository : ChatSource {

	private var remoteSource: ChatSource = DummyChatSource
	private var localSource: ChatSource? = null
	private val cacheSource = CacheChatSource

	override fun getSourceId() = remoteSource.getSourceId()

	override fun getAll(): Single<List<Chat>> {

//		cacheSource.getAll()
//			.takeUntil(remoteSource.getAll()
//				.flatMap { all ->
//					val userId = App.backend.getAuthManager().getCurrentUserId()
//					App.frontend.getCryptoManager(userId)
//						.decryptChats(all)
//						.flatMap {
//							cacheSource.reset(it)
//							localSource?.addItems(it)
//							Single.just(it)
//						}
//				})
//			.onErrorResumeNext {
//				Utils.logError(it)
//				localSource?.getAll()
//					?: Single.error(EmptyResultException)
//			}

		return remoteSource.getAll()
			.flatMap { all ->
				App.frontend.getCryptoManager(getSourceId())
					.decryptChats(all)
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

	override fun getItem(id: String, callback: RequestCallback<Chat>) {

		remoteSource.getItem(id, object : RequestCallback<Chat> {

			override fun onSuccess(result: Chat) {
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

	override fun addItem(item: Chat): Single<String> {

		return remoteSource.addItem(item)
			.doAfterSuccess {
				item.id = it
				cacheSource.addItem(item)
				localSource?.addItem(item)
			}
	}

	override fun addItems(items: List<Chat>, callback: RequestCallback<List<String>>) {

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

	override fun updateItem(item: Chat, callback: RequestCallback<Any>) {

		remoteSource.updateItem(item, object : RequestCallback<Any> {

			override fun onSuccess(result: Any) {
				cacheSource.updateItem(item)
				localSource?.updateItem(item)
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) { callback.onFailure(error) }
		})
	}

	override fun setMessageRecieved(id: String, callback: RequestCallback<Any>) {

		remoteSource.setMessageRecieved(id, object : RequestCallback<Any> {
			override fun onSuccess(result: Any) {
				cacheSource.setMessageRecieved(id)
				localSource?.setMessageRecieved(id)
			}
			override fun onFailure(error: Throwable) {
				callback.onFailure(error)
			}
		})
	}

	override fun getBroadcasts(callback: RequestCallback<List<Chat>>) {

		remoteSource.getBroadcasts(object : RequestCallback<List<Chat>> {

			override fun onSuccess(result: List<Chat>) {
				//TODO update local sources
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				Utils.logError(error)
				//TODO fallback
			}
		})
	}
	override fun attachListener(): Observable<List<Chat>> {

		return remoteSource.attachListener()
			.flatMap { chats ->
				App.frontend.getCryptoManager(getSourceId())
					.decryptChats(chats)
					.toObservable()
			}
			.flatMap {
				cacheSource.reset(it)
				localSource?.addItems(it)

				Observable.just(it)
			}
//			.onErrorResumeNext {
//				Utils.logError(it)
//				cacheSource.getAll().toObservable()
//			}
//			.onErrorResumeNext {
//				Utils.logError(it)
//				localSource?.getAll()
//					?.flatMap {
//						cacheSource.reset(it)
//						Single.just(it)
//					}
//					?.toObservable()
//					?: Observable.error(EmptyResultException)
//			}
	}

	override fun attachListener(id: String, callback: RequestCallback<Chat>): RepositorySubscription {

		return remoteSource.attachListener(id, object : RequestCallback<Chat> {

			override fun onSuccess(result: Chat) {
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

	fun setRemoteSource(source: ChatSource) {
		if (remoteSource == source)
			return

		remoteSource.clean()
		cacheSource.clear()

		remoteSource = source

		localSource?.let {
			if (source.getSourceId() != it.getSourceId()) {
				it.clean()
				localSource = null
			}
		}
	}

	fun setLocalSource(source: ChatSource) {
		if (localSource == source)
			return

		localSource?.clean()

		localSource = source

		if (source.getSourceId() != remoteSource.getSourceId()) {
			cacheSource.clear()
			remoteSource.clean()
			remoteSource = DummyChatSource
		}
	}

	private fun getItemFallback(id: String, callback: RequestCallback<Chat>) {

		cacheSource.getItem(id, object : RequestCallback<Chat> {

			override fun onSuccess(result: Chat) {
				callback.onSuccess(result)
			}
			override fun onFailure(error: Throwable) {
				localSource
					?.run { getItem(id, object : RequestCallback<Chat> {

						override fun onSuccess(result: Chat) {
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
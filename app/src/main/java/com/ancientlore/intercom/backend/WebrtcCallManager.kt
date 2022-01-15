package com.ancientlore.intercom.backend

import com.ancientlore.intercom.App
import com.ancientlore.intercom.C
import com.ancientlore.intercom.data.model.call.Answer
import com.ancientlore.intercom.data.model.call.Candidate
import com.ancientlore.intercom.data.model.call.Offer
import com.ancientlore.intercom.utils.Runnable1
import org.webrtc.*
import java.lang.RuntimeException
import java.util.ArrayList

/* Stun servers avaibility checker:
 * https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/
 */
abstract class WebrtcCallManager : CallManager<SurfaceViewRenderer> {

	companion object {
		const val TAG = C.CALLS_LOG_TAG

		const val FIELD_SDP = "sdp"
		const val FIELD_CALLER_ID = "callerId"
		const val FIELD_CALL_TYPE = "callType"
		const val FIELD_ID = "id"
		const val FIELD_LABEL = "label"
		const val FIELD_CANDIDATE = "candidate"

		private const val VIDEO_WIDTH = 1280
		private const val VIDEO_HEIGHT = 720
		private const val VIDEO_FPS = 30

		private const val MEDIA_STREAM_LABLE = "rtc_m0"
		private const val TRACK_LOCAL_AUDIO_ID = "rtc_a0"
		private const val TRACK_LOCAL_VIDEO_ID = "rtc_v0"

		private const val CAPTURE_THREAD_NAME = "captureThread"

		private val stunUriList = listOf( // TODO Maybe better to load from firebase
			"stun:stun.l.google.com:19302",
			"stun:stun1.l.google.com:19302",
			"stun:stun2.l.google.com:19302",
			"stun:stun3.l.google.com:19302",
			"stun:stun4.l.google.com:19302"
		)
	}

	private var connectionListener: CallConnectionListener? = null

	private val rootEglBase: EglBase by lazy { EglBase.create() }

	private var peerConnection: PeerConnection? = null
	private var localVideoTrack: VideoTrack? = null
	private var localAudioTrack: AudioTrack? = null
	private var videoCapturer: VideoCapturer? = null

	private var answerListenerSub: RepositorySubscription? = null
	private var offerListenerSub: RepositorySubscription? = null
	private var candidateListenerSub: RepositorySubscription? = null

	private val factory: PeerConnectionFactory by lazy {
		PeerConnectionFactory.initialize(
			PeerConnectionFactory.InitializationOptions
				.builder(App.context)
				.createInitializationOptions())
		PeerConnectionFactory.builder()
			.setVideoDecoderFactory(
				DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
			.setVideoEncoderFactory(
				DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, false))
			.createPeerConnectionFactory()
	}

	private val iceServers: List<PeerConnection.IceServer> by lazy {
		ArrayList<PeerConnection.IceServer>().apply {
			for (stunUri in stunUriList) {
				add(PeerConnection.IceServer.builder(stunUri)
					.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
					.createIceServer())
			}
		}
	}

	private val sdpMediaConstraints: MediaConstraints by lazy {
		MediaConstraints().apply {
			mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
			mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
		}
	}

	protected val userId get() = App.backend.getAuthManager().getCurrentUser().id

	abstract fun attachAnswerListener(targetId: String, callback: RequestCallback<Answer>) : RepositorySubscription

	abstract fun attachOfferListener(callback: RequestCallback<Offer>) : RepositorySubscription

	abstract fun attachCandidateListener(targetId: String, type: SessionDescription.Type, callback: RequestCallback<Candidate>) : RepositorySubscription

	abstract fun sendOffer(targetId: String, offer: HashMap<*,*>)

	abstract fun sendAnswer(targetId: String, answer: HashMap<*,*>)

	abstract fun sendCandidate(targetId: String, type: SessionDescription.Type, candidate: HashMap<*,*>)

	override fun dispose() {

		peerConnection = null
		localVideoTrack = null
		localAudioTrack = null
		videoCapturer = null
		connectionListener = null

		offerListenerSub?.remove()
		offerListenerSub = null
		answerListenerSub?.remove()
		answerListenerSub = null
		candidateListenerSub?.remove()
		candidateListenerSub = null

		rootEglBase.release()
		factory.dispose()
	}

	override fun setCallConnectionListener(listener: CallConnectionListener?) {
		this.connectionListener = listener
	}

	override fun setIncomingCallHandler(callback: Runnable1<Offer>) {
		offerListenerSub?.remove()
		offerListenerSub = attachOfferListener(object : CrashlyticsRequestCallback<Offer>(TAG) {
			override fun onSuccess(result: Offer) {
				callback.run(result)
			}
		})
	}

	override fun hungup() : Boolean {
		Logging.d(TAG, "hungup")

		answerListenerSub?.remove()
		candidateListenerSub?.remove()

		localVideoTrack = null
		localAudioTrack = null

		videoCapturer?.apply {
			stopCapture()
			dispose()
		}
		videoCapturer = null

		return peerConnection?.run {
			Logging.d(TAG, "hungup: dispose peerConnection")
			dispose()
			peerConnection = null
			true
		} ?: false
	}

	override fun call(targetId: String, videoViews: CallManager.VideoViews<SurfaceViewRenderer>?) {

		val offerType = videoViews?.run {
			localVideoView.apply {
				init(rootEglBase.eglBaseContext, null)
				setEnableHardwareScaler(true)
				setZOrderOnTop(true)
				setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
				setMirror(true)
			}
			remoteVideoView.apply {
				init(rootEglBase.eglBaseContext, null)
				setEnableHardwareScaler(true)
				setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
				setMirror(true)
			}

			Offer.CALL_TYPE_VIDEO
		} ?: Offer.CALL_TYPE_AUDIO

	}

	override fun call(params: CallManager.CallParams<SurfaceViewRenderer>) {
		Logging.d(TAG, "call: ${params.targetId}")

		init(SessionDescription.Type.OFFER, params)

		sendOffer(params.targetId, Offer.CALL_TYPE_VIDEO)
	}

	override fun call(params: CallManager.AudioCallParams) {

		init(SessionDescription.Type.OFFER, params)

		sendOffer(params.targetId, Offer.CALL_TYPE_AUDIO)
	}

	private fun sendOffer(targetId: String, callType: Int) {

		Logging.d(TAG, "createOffer: $targetId")
		peerConnection!!.createOffer(object : SimpleSdpObserver() {
			override fun onCreateSuccess(sessionDescription: SessionDescription) {
				Logging.d(TAG, "onCreateSuccess: ${sessionDescription.type}")
				peerConnection!!.setLocalDescription(SimpleSdpObserver(), sessionDescription)

				sendOffer(targetId, hashMapOf(
					FIELD_CALLER_ID to userId,
					FIELD_CALL_TYPE to callType,
					FIELD_SDP to sessionDescription.description
				))
				candidateListenerSub?.remove()
				candidateListenerSub = attachCandidateListener(
					targetId,
					sessionDescription.type,
					object : CrashlyticsRequestCallback<Candidate>(TAG) {
						override fun onSuccess(result: Candidate) {
							onCandidate(result)
						}
					})
			}
		}, sdpMediaConstraints)

		answerListenerSub?.remove()
		answerListenerSub = attachAnswerListener(targetId, object : CrashlyticsRequestCallback<Answer>() {
			override fun onSuccess(result: Answer) {
				onAnswer(result)
			}
		})
	}

	override fun answer(params: CallManager.CallParams<SurfaceViewRenderer>, sdp: String) {
		Logging.d(TAG, "answer: ${params.targetId}")

		init(SessionDescription.Type.ANSWER, params)

		sendAnswer(params.targetId, sdp)
	}

	override fun answer(params: CallManager.AudioCallParams, sdp: String) {

		init(SessionDescription.Type.ANSWER, params)

		sendAnswer(params.targetId, sdp)
	}

	private fun sendAnswer(targetId: String, sdp: String) {

		Logging.d(TAG, "onOffer: setRemoteDescription")
		peerConnection!!.setRemoteDescription(
			SimpleSdpObserver(),
			SessionDescription(SessionDescription.Type.OFFER, sdp))

		Logging.d(TAG, "createAnswer: $targetId")
		peerConnection!!.createAnswer(object : SimpleSdpObserver() {
			override fun onCreateSuccess(sessionDescription: SessionDescription) {
				Logging.d(TAG, "onCreateSuccess: ${sessionDescription.type}")
				peerConnection!!.setLocalDescription(SimpleSdpObserver(), sessionDescription)

				sendAnswer(targetId, hashMapOf(
					FIELD_SDP to sessionDescription.description
				))
				candidateListenerSub?.remove()
				candidateListenerSub = attachCandidateListener(
					targetId,
					sessionDescription.type,
					object : CrashlyticsRequestCallback<Candidate>(TAG) {
						override fun onSuccess(result: Candidate) {
							onCandidate(result)
						}
					})
			}
		}, MediaConstraints())
	}

	private fun init(type: SessionDescription.Type, params: CallManager.AudioCallParams) {

		localAudioTrack = factory.createAudioTrack(TRACK_LOCAL_AUDIO_ID,
			factory.createAudioSource(MediaConstraints()))

		val pcObserver: PeerConnection.Observer = object : PeerConnection.Observer {
			override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
				Logging.d(TAG, "onSignalingChange: ${signalingState.name}")
			}
			override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
				Logging.d(TAG, "onIceConnectionChange: ${iceConnectionState.name}")

				when (iceConnectionState) {
					PeerConnection.IceConnectionState.CONNECTED -> connectionListener?.onConnected()
					PeerConnection.IceConnectionState.DISCONNECTED -> connectionListener?.onDisconnected()
					else -> {}
				}
			}
			override fun onIceConnectionReceivingChange(b: Boolean) {
				Logging.d(TAG, "onIceConnectionReceivingChange: $b")
			}
			override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
				Logging.d(TAG, "onIceGatheringChange: $iceGatheringState")
			}
			override fun onIceCandidate(iceCandidate: IceCandidate) {
				Logging.d(TAG, "onIceCandidate: ${iceCandidate.sdpMid}")

				sendCandidate(params.targetId, type, hashMapOf(
					FIELD_LABEL to iceCandidate.sdpMLineIndex,
					FIELD_ID to iceCandidate.sdpMid,
					FIELD_CANDIDATE to iceCandidate.sdp
				))
			}
			override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
				Logging.d(TAG, "onIceCandidatesRemoved: ${iceCandidates.size}")
			}
			override fun onAddStream(mediaStream: MediaStream) {
				Logging.d(TAG, "onAddStream: ${mediaStream.id}")
				val remoteAudioTrack = mediaStream.audioTracks[0]
				remoteAudioTrack.setEnabled(true)
			}
			override fun onRemoveStream(mediaStream: MediaStream) {
				Logging.d(TAG, "onRemoveStream: ${mediaStream.id}")
			}
			override fun onDataChannel(dataChannel: DataChannel) {
				Logging.d(TAG, "onDataChannel: ${dataChannel.label()}")
			}
			override fun onRenegotiationNeeded() {
				Logging.d(TAG, "onRenegotiationNeeded")
			}
			override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState) {
				Logging.d(TAG, "onStandardizedIceConnectionChange: ${newState.name}")
			}
		}

		val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

		peerConnection = factory.createPeerConnection(rtcConfig, pcObserver)

		factory.createLocalMediaStream(MEDIA_STREAM_LABLE).run {
			addTrack(localAudioTrack)
			peerConnection!!.addStream(this)
		}
	}

	private fun init(type: SessionDescription.Type, params: CallManager.CallParams<SurfaceViewRenderer>) {

		params.localVideoView.apply {
			init(rootEglBase.eglBaseContext, null)
			setEnableHardwareScaler(true)
			setZOrderOnTop(true)
			setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
			setMirror(true)
		}
		params.remoteVideoView.apply {
			init(rootEglBase.eglBaseContext, null)
			setEnableHardwareScaler(true)
			setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
			setMirror(true)
		}

		initLocalTracks(params.localVideoView)

		initPeerConnection(params.targetId, type, params.remoteVideoView)

		startLocalStreaming()
	}

	private fun initLocalTracks(localVideoSink: VideoSink) {
		Logging.d(TAG, "initLocalTracks")

		val videoCapturer = createVideoCapturer()
		this.videoCapturer = videoCapturer

		val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
		videoCapturer.initialize(
			SurfaceTextureHelper.create(CAPTURE_THREAD_NAME, rootEglBase.eglBaseContext),
			App.context,
			videoSource.capturerObserver)

		localVideoTrack = factory.createVideoTrack(TRACK_LOCAL_VIDEO_ID, videoSource).apply {
			setEnabled(true)
			addSink(localVideoSink)
		}

		val audioSource = factory.createAudioSource(MediaConstraints())
		localAudioTrack = factory.createAudioTrack(TRACK_LOCAL_AUDIO_ID, audioSource)

		videoCapturer.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FPS)
	}

	private fun createVideoCapturer(): VideoCapturer {
		Logging.d(TAG, "createVideoCapturer")

		return if (Camera2Enumerator.isSupported(App.context))
			createCameraCapturer(Camera2Enumerator(App.context))
		else
			createCameraCapturer(Camera1Enumerator(true))
	}

	private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer {
		val deviceNames = enumerator.deviceNames

		var fallbackCapturer: VideoCapturer? = null
		for (deviceName in deviceNames) {
			if (enumerator.isFrontFacing(deviceName)) {
				val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
				if (videoCapturer != null) {
					Logging.d(TAG, "Using front camera capturer")
					return videoCapturer
				}
			}
			else {
				val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
				if (videoCapturer != null) {
					fallbackCapturer = videoCapturer
				}
			}
		}

		Logging.d(TAG, "Using not front camera capturer")
		if (fallbackCapturer != null)
			return fallbackCapturer

		Logging.e(TAG, "No cameras found")
		throw RuntimeException() // FIXME app shouldn't fall
	}

	private fun initPeerConnection(targetId: String, type: SessionDescription.Type, remoteVideoSink: VideoSink) {
		Logging.d(TAG, "initPeerConnection")

		val pcObserver: PeerConnection.Observer = object : PeerConnection.Observer {
			override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
				Logging.d(TAG, "onSignalingChange: ${signalingState.name}")
			}
			override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
				Logging.d(TAG, "onIceConnectionChange: ${iceConnectionState.name}")

				when (iceConnectionState) {
					PeerConnection.IceConnectionState.CONNECTED -> connectionListener?.onConnected()
					PeerConnection.IceConnectionState.DISCONNECTED -> connectionListener?.onDisconnected()
					else -> {}
				}
			}
			override fun onIceConnectionReceivingChange(b: Boolean) {
				Logging.d(TAG, "onIceConnectionReceivingChange: $b")
			}
			override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
				Logging.d(TAG, "onIceGatheringChange: $iceGatheringState")
			}
			override fun onIceCandidate(iceCandidate: IceCandidate) {
				Logging.d(TAG, "onIceCandidate: ${iceCandidate.sdpMid}")

				sendCandidate(targetId, type, hashMapOf(
					FIELD_LABEL to iceCandidate.sdpMLineIndex,
					FIELD_ID to iceCandidate.sdpMid,
					FIELD_CANDIDATE to iceCandidate.sdp
				))
			}
			override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
				Logging.d(TAG, "onIceCandidatesRemoved: ${iceCandidates.size}")
			}
			override fun onAddStream(mediaStream: MediaStream) {
				Logging.d(TAG, "onAddStream: ${mediaStream.id}")
				val remoteAudioTrack = mediaStream.audioTracks[0]
				remoteAudioTrack.setEnabled(true)
				val remoteVideoTrack = mediaStream.videoTracks[0]
				remoteVideoTrack.setEnabled(true)
				remoteVideoTrack.addSink(remoteVideoSink)
			}
			override fun onRemoveStream(mediaStream: MediaStream) {
				Logging.d(TAG, "onRemoveStream: ${mediaStream.id}")
			}
			override fun onDataChannel(dataChannel: DataChannel) {
				Logging.d(TAG, "onDataChannel: ${dataChannel.label()}")
			}
			override fun onRenegotiationNeeded() {
				Logging.d(TAG, "onRenegotiationNeeded")
			}
			override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState) {
				Logging.d(TAG, "onStandardizedIceConnectionChange: ${newState.name}")
			}
		}

		val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

		peerConnection = factory.createPeerConnection(rtcConfig, pcObserver)
	}

	private fun startLocalStreaming() {
		Logging.d(TAG, "startLocalStreaming")
		val mediaStream = factory.createLocalMediaStream(MEDIA_STREAM_LABLE)
		mediaStream.addTrack(localVideoTrack)
		mediaStream.addTrack(localAudioTrack)
		peerConnection!!.addStream(mediaStream)
	}

	private fun onAnswer(answer: Answer) {
		Logging.d(TAG, "onAnswer: setRemoteDescription")
		peerConnection!!.setRemoteDescription(
			SimpleSdpObserver(),
			SessionDescription(SessionDescription.Type.ANSWER, answer.sdp))
	}

	private fun onCandidate(candidate: Candidate) {
		Logging.d(TAG, "onCandidate: addIceCandidate")
		peerConnection!!.addIceCandidate(IceCandidate(
			candidate.id,
			candidate.label,
			candidate.candidate))
	}
}
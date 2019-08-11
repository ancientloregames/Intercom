package com.ancientlore.intercom.ui.notification

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ancientlore.intercom.BR
import com.ancientlore.intercom.NotificationActionReceiver
import com.ancientlore.intercom.NotificationActionReceiver.Companion.RESULT_REPLY
import com.ancientlore.intercom.R
import com.ancientlore.intercom.data.model.PushMessage
import com.ancientlore.intercom.databinding.NotificationAnswerUiBinding
import com.ancientlore.intercom.utils.NotificationManager
import io.reactivex.internal.disposables.ListCompositeDisposable
import kotlinx.android.synthetic.main.notification_answer_ui.*

class NotificationAnswerActivity: AppCompatActivity() {

	private val subscriptions = ListCompositeDisposable()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val viewDataBinding : NotificationAnswerUiBinding = DataBindingUtil.setContentView(this, R.layout.notification_answer_ui)
		viewDataBinding.lifecycleOwner = this
		val viewModel = NotificationAnswerViewModel(message)
		viewDataBinding.setVariable(BR.ui, viewModel)
		viewDataBinding.executePendingBindings()
		subscriptions.add(viewModel.observeSendClicked()
			.subscribe { onSend(it) })
		subscriptions.add(viewModel.observeCancelClicked()
			.subscribe { finish() })

		if (replyEditView.requestFocus())
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
	}

	override fun onStop() {
		finish()
		super.onStop()
	}

	private fun onSend(replyText: String) {
		val extras = intent.extras!!.apply {
			putString(RESULT_REPLY, replyText)
		}
		val replyIntent = Intent(NotificationActionReceiver.ACTION_REPLY).apply {
			putExtras(extras)
		}
		sendBroadcast(replyIntent)
		finish()
	}

	private val message: PushMessage get() = intent.getParcelableExtra<PushMessage>(NotificationManager.EXTRA_MESSAGE)
		?: throw RuntimeException("Message mandatory!")
}
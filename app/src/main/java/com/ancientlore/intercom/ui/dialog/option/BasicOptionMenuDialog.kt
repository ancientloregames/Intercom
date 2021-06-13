package com.ancientlore.intercom.ui.dialog.option

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ancientlore.intercom.ui.BasicViewModel
import com.ancientlore.intercom.utils.extensions.runOnUiThread
import io.reactivex.internal.disposables.ListCompositeDisposable

abstract class BasicOptionMenuDialog<T: BasicViewModel>: DialogFragment() {

	protected lateinit var viewModel: T

	protected val subscriptions = ListCompositeDisposable()

	@LayoutRes
	abstract fun getLayoutResId() : Int

	abstract fun setupViewModel(view: View)

	fun show(manager: FragmentManager) {
		show(manager, javaClass.simpleName)
	}

	override fun show(manager: FragmentManager, tag: String?) {
		val transaction = manager.beginTransaction()
		manager.findFragmentByTag(tag)
			?.let { transaction.remove(it) }
		transaction.addToBackStack(null)

		runOnUiThread {
			show(transaction, tag)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(getLayoutResId(), container)

		setStyle(STYLE_NO_FRAME, android.R.style.Theme)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

		setupViewModel(view)

		subscribeOnViewModel()

		return view
	}

	open fun subscribeOnViewModel() {}

	override fun onDestroyView() {
		viewModel.clean()
		subscriptions.dispose()

		super.onDestroyView()
	}
}
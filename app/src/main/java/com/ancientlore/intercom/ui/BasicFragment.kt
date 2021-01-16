package com.ancientlore.intercom.ui

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.ancientlore.intercom.MainActivity
import com.ancientlore.intercom.utils.PermissionManager
import io.reactivex.internal.disposables.ListCompositeDisposable
import java.lang.RuntimeException

abstract class BasicFragment<VM : BasicViewModel, B : ViewDataBinding> : Fragment(), MainActivity.BackButtonHandler {

	override fun onBackPressed() = false

	protected lateinit var viewModel: VM
	protected lateinit var dataBinding: B

	protected val subscriptions = ListCompositeDisposable()

	protected val navigator get() = activity as Navigator?

	protected val permissionManager get() = activity as PermissionManager?

	@LayoutRes
	protected abstract fun getLayoutResId(): Int

	protected abstract fun createViewModel(): VM

	protected abstract fun bind(view: View, viewModel: VM)

	protected abstract fun initViewModel(viewModel: VM)

	protected abstract fun observeViewModel(viewModel: VM)

	final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
		return inflater.inflate(getLayoutResId(), container, false)
	}

	override fun onAttach(context: Context) {
		if (context !is Navigator)
			RuntimeException("Context must implement the Navigator interface")
		super.onAttach(context)
	}

	final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		initView(view, savedInstanceState)

		viewModel = createViewModel()

		bind(view, viewModel)

		initViewModel(viewModel)
		observeViewModel(viewModel)
	}

	protected open fun initView(view: View, savedInstanceState: Bundle?) {}

	override fun onDestroyView() {
		subscriptions.clear()
		super.onDestroyView()
	}

	protected fun runOnUiThread(action: Runnable) {
		activity?.runOnUiThread(action)
	}

	protected fun showToast(@StringRes textResId: Int) {
		runOnUiThread(Runnable {
			Toast.makeText(context, textResId, Toast.LENGTH_LONG).show()
		})
	}

	@CallSuper
	protected open fun close() {
		viewModel.clean()
		navigator?.closeFragment(this)
	}
}
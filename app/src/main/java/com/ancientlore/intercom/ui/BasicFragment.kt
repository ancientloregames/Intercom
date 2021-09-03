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
import com.ancientlore.intercom.R
import com.ancientlore.intercom.utils.PermissionManager
import com.ancientlore.intercom.utils.Utils
import com.ancientlore.intercom.widget.SimpleAnimationListener
import io.reactivex.internal.disposables.ListCompositeDisposable
import java.lang.RuntimeException

import android.content.res.Resources
import android.view.animation.Animation
import android.view.animation.AnimationUtils

abstract class BasicFragment<VM : BasicViewModel, B : ViewDataBinding> : Fragment(), MainActivity.BackButtonHandler {

	override fun onBackPressed() = false

	protected lateinit var viewModel: VM
	protected lateinit var dataBinding: B

	protected val subscriptions = ListCompositeDisposable()

	protected val navigator get() = activity as Navigator?

	protected val permissionManager get() = activity as PermissionManager?

	@Volatile
	private var isClosing = false

	@LayoutRes
	protected abstract fun getLayoutResId(): Int

	protected abstract fun createViewModel(): VM

	protected abstract fun bind(view: View, viewModel: VM)

	protected abstract fun initViewModel(viewModel: VM)

	open fun getOpenAnimation(): Int = R.anim.slide_in_right

	open fun getCloseAnimation(): Int = R.anim.slide_out_right

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

	@CallSuper
	protected open fun observeViewModel(viewModel: VM) {
		subscriptions.add(viewModel.observeToastRequest()
			.subscribe { showToast(it) })
	}

	protected open fun initView(view: View, savedInstanceState: Bundle?) {}

	override fun onDestroyView() {
		subscriptions.clear()
		viewModel.clean()
		super.onDestroyView()
	}

	protected fun runOnUiThread(action: Runnable) {
		activity?.runOnUiThread(action)
	}

	protected fun showToast(@StringRes textResId: Int, duration: Int = Toast.LENGTH_LONG) {
		runOnUiThread {
			Toast.makeText(context, textResId, duration).show()
		}
	}

	protected fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
		runOnUiThread {
			Toast.makeText(context, message, duration).show()
		}
	}

	@CallSuper
	protected open fun close(animate: Boolean = true) {
		if (isClosing)
			return
		isClosing = true

		if (!animate || context == null)
			closeNow()

		try {
			AnimationUtils.loadAnimation(context, getCloseAnimation())
				.run {
					setAnimationListener(object : SimpleAnimationListener() {
						override fun onAnimationEnd(animation: Animation?) {
							super.onAnimationEnd(animation)
							closeNow()
						}
					})
					view?.startAnimation(this)
					start()
				}
		} catch (e: Resources.NotFoundException) {
			Utils.logError(e)
			closeNow()
		}
	}

	protected fun closeNow() {
		navigator?.closeFragment(this@BasicFragment)
	}
}
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
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.UiThread
import java.util.ArrayList

abstract class BasicFragment<VM : BasicViewModel, B : ViewDataBinding> : Fragment(), MainActivity.BackButtonHandler {

	protected lateinit var viewModel: VM
	protected lateinit var dataBinding: B

	protected val subscriptions = ListCompositeDisposable()

	protected val navigator get() = activity as Navigator?

	protected val permissionManager get() = activity as PermissionManager?

	@Volatile
	private var isClosing = false

	@LayoutRes
	protected abstract fun getLayoutResId(): Int

	protected abstract fun createDataBinding(view: View): B

	protected abstract fun createViewModel(): VM

	override fun onBackPressed(): Boolean {
		close()
		return true
	}

	open fun getOpenAnimation(): Int = R.anim.slide_in_right

	open fun getCloseAnimation(): Int = R.anim.slide_out_right

	override fun onAttach(context: Context) {
		if (context !is Navigator || context !is PermissionManager)
			throw RuntimeException("Context must implement the Navigator and PermissionManager interfaces")
		super.onAttach(context)
	}

	/**
	 * FIXME  this method is no longer valid for the newer versions of the fragment library (1.2.0+)
	 *        need to find a better one. Maybe to use the toolbar listeners directly and avoid using
	 *        the activity option menu
	 *
	 * SDK error that leads to the fragment leaking because of the ActionBar option menu handling
	 * @see setHasOptionsMenu
	 *
	 * @see <a href="https://issuetracker.google.com/issues/131537919">Google issue tracker</a>
	 */
	override fun onDetach() {
		super.onDetach()

		fragmentManager?.let { fragmentManager ->
			try {
				with(fragmentManager.javaClass.getDeclaredField("mCreatedMenus")) {
					isAccessible = true
					val createdMenus = get(fragmentManager)
					if (createdMenus is ArrayList<*>) {
						if (createdMenus.remove(this@BasicFragment))
							Log.d("Intercom", "Fragment successfully removed from the mCreatedMenus")
					}
				}
			} catch (e: NoSuchFieldException) {
				e.printStackTrace()
			} catch (e: SecurityException) {
				e.printStackTrace()
			} catch (e: IllegalAccessException) {
				e.printStackTrace()
			}
		}
	}

	final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
		return inflater.inflate(getLayoutResId(), container, false)
	}

	final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		dataBinding = createDataBinding(view)

		viewModel = createViewModel()

		init(viewModel, savedInstanceState)
	}

	@CallSuper
	protected open fun init(viewModel: VM, savedState: Bundle?) {

		subscriptions.add(viewModel.observeToastRequest()
			.subscribe {
				val stringRes = getToastStringRes(it)
				if (stringRes != -1)
					showToast(stringRes)
			})
	}

	override fun onDestroyView() {
		subscriptions.clear()
		viewModel.clean()
		dataBinding.unbind()
		super.onDestroyView()
	}

	protected fun runOnUiThread(action: Runnable) {
		activity?.runOnUiThread(action)
	}

	@StringRes
	protected open fun getToastStringRes(toastId: Int): Int  = -1

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

	protected open fun close(animate: Boolean = true) {
		if (isClosing)
			return
		isClosing = true

		if (!animate || context == null)
			closeNow()

		runOnUiThread {
			closeWithAnimation()
		}
	}

	@UiThread
	private fun closeWithAnimation() {

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
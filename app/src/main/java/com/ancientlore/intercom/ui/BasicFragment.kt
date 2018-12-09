package com.ancientlore.intercom.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.ancientlore.intercom.backend.BackendManager
import io.reactivex.internal.disposables.ListCompositeDisposable
import java.lang.RuntimeException

abstract class BasicFragment<VM : ViewModel, B : ViewDataBinding> : Fragment() {

	protected lateinit var viewModel: VM
	protected lateinit var dataBinding: B

	protected val subscriptions = ListCompositeDisposable()

	protected val backend get() = (context as BackendManager).getBackend()

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
		if (context !is BackendManager)
			RuntimeException("Context must implement the BackendManager interface")
		super.onAttach(context)
	}

	final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val viewModel = createViewModel()

		bind(view, viewModel)

		initViewModel(viewModel)
		observeViewModel(viewModel)
	}

	override fun onDestroyView() {
		subscriptions.clear()
		super.onDestroyView()
	}
}
package com.ancientlore.intercom.ui

import androidx.lifecycle.ViewModel
import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import io.reactivex.internal.disposables.ListCompositeDisposable

abstract class BasicFragment<VM : ViewModel, B : ViewDataBinding> : Fragment() {

	protected lateinit var viewModel: VM
	protected lateinit var dataBinding: B

	protected val subscriptions = ListCompositeDisposable()

	@LayoutRes
	protected abstract fun getLayoutResId(): Int

	protected abstract fun createViewModel(): VM

	protected abstract fun bind(view: View, viewModel: VM)

	protected abstract fun initViewModel(viewModel: VM)

	protected abstract fun observeViewModel(viewModel: VM)

	final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
		return inflater.inflate(getLayoutResId(), container, false)
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
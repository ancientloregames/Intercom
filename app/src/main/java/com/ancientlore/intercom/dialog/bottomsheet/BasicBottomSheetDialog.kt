package com.ancientlore.intercom.dialog.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.AnyThread
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BasicBottomSheetDialog: BottomSheetDialogFragment() {

	protected abstract fun getFragmentTag(): String

	@LayoutRes
	protected abstract fun getLayoutResId(): Int

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
			= inflater.inflate(getLayoutResId(), container, false)!!

	fun show(manager: FragmentManager) {
		val transaction = manager.beginTransaction()
		manager.findFragmentByTag(getFragmentTag())
			?.let { transaction.remove(it) }
		transaction.addToBackStack(null)

		show(transaction, getFragmentTag())
	}

	@AnyThread
	fun hide() = activity?.runOnUiThread { dismiss() }
}
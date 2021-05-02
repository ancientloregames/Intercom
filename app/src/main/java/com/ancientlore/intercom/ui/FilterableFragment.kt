package com.ancientlore.intercom.ui

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.annotation.CallSuper
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.R
import com.ancientlore.intercom.utils.Runnable1

abstract class FilterableFragment<VM : FilterableViewModel<*>, B : ViewDataBinding> : BasicFragment<VM, B>() {

	private val menuCallback = Runnable1<Menu> { menu ->
		activity?.menuInflater?.inflate(getToolbarMenuResId(), menu)
		val search = menu.findItem(R.id.search).actionView as SearchView
		search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String?): Boolean {
				query?.let { constraint ->
					viewModel.filter(constraint)
				}
				return true
			}

			override fun onQueryTextChange(newText: String?): Boolean {
				newText
					?.takeIf { it.length > 1 }
					?.let { viewModel.filter(it) }
					?: run { viewModel.filter("") }
				return true
			}
		})
	}

	abstract fun getToolbar() : Toolbar

	@MenuRes
	abstract fun getToolbarMenuResId() : Int

	@CallSuper
	override fun initView(view: View, savedInstanceState: Bundle?) {
		super.initView(view, savedInstanceState)

		navigator?.createToolbarMenu(getToolbar(), menuCallback)
	}
}
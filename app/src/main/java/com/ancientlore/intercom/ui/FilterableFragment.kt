package com.ancientlore.intercom.ui

import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.ancientlore.intercom.R
import com.ancientlore.intercom.utils.Runnable1

abstract class FilterableFragment<VM : FilterableViewModel<*>, B : ViewDataBinding> : BasicFragment<VM, B>() {

	private var search: SearchView? = null

	private val menuCallback = Runnable1<Menu> { menu ->
		activity?.menuInflater?.inflate(getToolbarMenuResId(), menu)
		search = menu.findItem(R.id.search).actionView as SearchView
		search!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String?): Boolean {
				query?.let { constraint ->
					requestViewModel().filter(constraint)
				}
				return true
			}

			override fun onQueryTextChange(newText: String?): Boolean {
				newText
					?.takeIf { it.length > 1 }
					?.let { requestViewModel().filter(it) }
					?: run { requestViewModel().filter("") }
				return true
			}
		})
	}

	abstract fun getToolbar() : Toolbar

	@MenuRes
	abstract fun getToolbarMenuResId() : Int

	override fun init(savedState: Bundle?) {
		super.init(savedState)

		navigator?.createToolbarMenu(getToolbar(), menuCallback)
	}

	override fun onDestroyView() {
		setHasOptionsMenu(false)
		search?.setOnQueryTextListener(null)
		(activity as AppCompatActivity).setSupportActionBar(null)
		super.onDestroyView()
	}
}
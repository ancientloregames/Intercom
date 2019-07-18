package com.ancientlore.intercom.widget.list.simple

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.ancientlore.intercom.widget.list.BasicListAdapter
import com.ancientlore.intercom.databinding.SimpleListItemBinding

class SimpleListAdapter(context: Context)
	: BasicListAdapter<SimpleListItem>(context),
	SimpleListItemViewModel.Listener {

	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val  binding = convertView?.let { view ->
			DataBindingUtil.getBinding<SimpleListItemBinding>(view)
		} ?: SimpleListItemBinding.inflate(inflater, parent, false)

		val viewModel = SimpleListItemViewModel(getItem(position))
		binding.viewModel = viewModel

		viewModel.setListener(this)

		return binding.root
	}

	override fun onItemClick(item: SimpleListItem) = itemClickedEvent.onNext(item)
}
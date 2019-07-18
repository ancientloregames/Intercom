package com.ancientlore.intercom.widget.list.simple

import com.ancientlore.intercom.widget.list.BasicListItemViewModel

class SimpleListItemViewModel(item: SimpleListItem): BasicListItemViewModel<SimpleListItem>(item) {
	interface Listener: BasicListItemViewModel.Listener<SimpleListItem>
}
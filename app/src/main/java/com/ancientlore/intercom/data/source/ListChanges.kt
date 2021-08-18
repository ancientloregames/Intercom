package com.ancientlore.intercom.data.source

import java.util.LinkedList

data class ListChanges<I>(val addList: LinkedList<I>,
                          val modifyList: LinkedList<I>,
                          val removeList: LinkedList<I>) {

  fun isEmpty() = addList.isEmpty() && modifyList.isEmpty() && removeList.isEmpty()

  fun isNotEmpty() = !isEmpty()
}
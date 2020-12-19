package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account

const val SPLIT_CATEGORY_NAME = "Split/Multiple Categories"

sealed class DelayedCategory {

    abstract val name: String

    class Transfer(val account: Account) : DelayedCategory() {
        override val name = "Transfer : ${account.name}"
    }

    class Pending(string: String) : DelayedCategory() {
        override val name = string
    }

    object Split : DelayedCategory() {
        override val name = SPLIT_CATEGORY_NAME
    }
}

object DelayedCategoryComparator : Comparator<DelayedCategory> {
    override fun compare(o1: DelayedCategory?, o2: DelayedCategory?): Int =
            when {
                o1 == o2 -> 0
                o1 == null -> 1
                o2 == null -> -1
                o1 is DelayedCategory.Transfer && o2 !is DelayedCategory.Transfer -> 1
                o2 is DelayedCategory.Transfer && o1 !is DelayedCategory.Transfer -> -1
                else -> o1.name.compareTo(o2.name)
            }
}

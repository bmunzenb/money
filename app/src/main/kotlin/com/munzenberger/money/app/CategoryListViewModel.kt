package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.CompositeSubscription
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXCategory
import com.munzenberger.money.app.model.getAllWithParent
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Category

class CategoryListViewModel : AutoCloseable {

    private val categories = SimpleAsyncObjectProperty<List<FXCategory>>()

    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<FXCategory>> = categories

    private val subscriptions = CompositeSubscription()

    fun start(database: ObservableMoneyDatabase) {
        database.subscribeOnUpdate {
            categories.setValueAsync {
                Category.getAllWithParent(database)
                        .map { FXCategory(it.category, it.parentName) }
                        .sortedBy { it.nameProperty.value }
            }
        }.also { subscriptions.add(it) }
    }

    override fun close() {
        subscriptions.cancel()
    }
}

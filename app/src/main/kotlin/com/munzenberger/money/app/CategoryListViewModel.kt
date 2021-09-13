package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXCategory
import com.munzenberger.money.app.model.getAllWithParent
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Category
import io.reactivex.rxjava3.disposables.CompositeDisposable

class CategoryListViewModel : AutoCloseable {

    private val categories = SimpleAsyncObjectProperty<List<FXCategory>>()

    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<FXCategory>> = categories

    private val disposables = CompositeDisposable()

    fun start(database: ObservableMoneyDatabase) {
        database.onUpdate.subscribe {
            categories.setValueAsync {
                Category.getAllWithParent(database)
                        .map { FXCategory(it.category, it.parentName) }
                        .sortedBy { it.nameProperty.value }
            }
        }.also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}

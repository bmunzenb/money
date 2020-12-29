package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXCategory
import com.munzenberger.money.app.model.getAllWithParent
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.flatMapAsyncObject
import com.munzenberger.money.core.Category
import io.reactivex.rxjava3.disposables.CompositeDisposable

class CategoryListViewModel : AutoCloseable {

    private val categories = SimpleAsyncObjectProperty<List<FXCategory>>()

    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<FXCategory>> = categories

    private val disposables = CompositeDisposable()

    fun start(database: ObservableMoneyDatabase) {

        database.onUpdate.flatMapAsyncObject {
            Category.getAllWithParent(database)
                    .map { FXCategory(it.category, it.parentName) }
                    .sortedBy { it.nameProperty.value }
        }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe { categories.value = it }
                .also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}

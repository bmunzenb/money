package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXCategory
import com.munzenberger.money.app.model.getAllWithParent
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.flatMapAsyncObject
import com.munzenberger.money.core.Category
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javafx.scene.control.TreeItem

class CategoryListViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val categories = SimpleAsyncObjectProperty<TreeItem<FXCategory>>()

    val categoriesProperty: ReadOnlyAsyncObjectProperty<TreeItem<FXCategory>> = categories

    fun start(database: ObservableMoneyDatabase) {

        database.onUpdate.flatMapAsyncObject { loadCategories(database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe { categories.value = it }
                .also { disposables.add(it) }
    }

    private fun loadCategories(database: ObservableMoneyDatabase): TreeItem<FXCategory> {

        val root = TreeItem<FXCategory>(FXCategory.Root)

        val list = Category.getAllWithParent(database)

        return root
    }

    override fun close() {
        disposables.clear()
    }
}

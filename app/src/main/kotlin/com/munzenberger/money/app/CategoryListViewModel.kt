package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.CategoryWithParent
import com.munzenberger.money.app.model.FXCategory
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.flatMapAsyncObject
import com.munzenberger.money.app.property.map
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem

class CategoryListViewModel : AutoCloseable {

    private val root = SimpleObjectProperty<TreeItem<FXCategory>>()
    private val categories = SimpleAsyncObjectProperty<List<CategoryWithParent>>()

    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<CategoryWithParent>> = categories
    val rootProperty: ReadOnlyObjectProperty<TreeItem<FXCategory>> = root

    fun start(database: ObservableMoneyDatabase) {

        database.onUpdate.flatMapAsyncObject { loadCategories(database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe { async ->
                    categories.value = async.map { it.first }
                    root.value = when (async) {
                        is AsyncObject.Complete -> async.value.second
                        else -> null
                    }
                }
    }

    private fun loadCategories(database: ObservableMoneyDatabase): Pair<List<CategoryWithParent>, TreeItem<FXCategory>> {
        TODO()
    }

    override fun close() {
        // do nothing
    }
}

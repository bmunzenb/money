package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.CategoryWithParent
import com.munzenberger.money.app.model.FXCategory
import com.munzenberger.money.app.model.getAllWithParent
import com.munzenberger.money.app.property.AsyncObjectMapper
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.flatMapAsyncObject
import com.munzenberger.money.core.Category
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem

class CategoryListViewModel : AutoCloseable {

    private val root = SimpleObjectProperty<TreeItem<FXCategory>>()
    private val categories = SimpleAsyncObjectProperty<List<CategoryWithParent>>()

    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<CategoryWithParent>> = categories
    val rootProperty: ReadOnlyObjectProperty<TreeItem<FXCategory>> = root

    init {

       val mapper = object : AsyncObjectMapper<List<CategoryWithParent>, TreeItem<FXCategory>?> {

           override fun pending(): TreeItem<FXCategory>? = null

           override fun executing(): TreeItem<FXCategory>? = null

           override fun error(error: Throwable): TreeItem<FXCategory>? = null

           override fun complete(obj: List<CategoryWithParent>): TreeItem<FXCategory> {

               

               return TreeItem(FXCategory.Root)
           }
       }

        root.bindAsync(categoriesProperty, mapper)
    }

    fun start(database: ObservableMoneyDatabase) {

        database.onUpdate.flatMapAsyncObject { Category.getAllWithParent(database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe { categories.value = it }
    }

    override fun close() {
        // do nothing
    }
}

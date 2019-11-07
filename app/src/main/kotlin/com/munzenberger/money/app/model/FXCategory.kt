package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

class FXCategory(private val category: Category) {

    val unwrap = category

    val accountNameProperty: ReadOnlyStringProperty = SimpleStringProperty(category.account?.name)
    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(category.name)

    val transactionalName: String by lazy {

        val pair = category.account?.name to category.name

        when {
            pair.first == null -> "<none>"
            pair.second == null -> when (category.account?.accountType?.category) {
                AccountType.Category.ASSETS, AccountType.Category.LIABILITIES -> "Transfer : ${pair.first}"
                else -> "${pair.first}"
            }
            else -> "${pair.first} : ${pair.second}"
        }
    }

    companion object {

        fun getAll(database: MoneyDatabase) = Category.getAll(database).map {
            // TODO: sort the categories properly
            it.map { c -> FXCategory(c) }.sortedBy { c -> c.transactionalName }
        }
    }
}

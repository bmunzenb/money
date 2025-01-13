package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import com.munzenberger.money.core.model.CategoryType
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

class FXCategory(
    category: Category,
    parentName: String?,
) {
    val nameProperty: ReadOnlyStringProperty
    val typeProperty: ReadOnlyStringProperty

    init {

        val name =
            when (parentName) {
                null -> category.name
                else -> "$parentName $CATEGORY_DELIMITER ${category.name}"
            }

        nameProperty = SimpleStringProperty(name)

        val type =
            when (category.type) {
                CategoryType.INCOME -> "Income"
                CategoryType.EXPENSE -> "Expense"
                else -> null
            }

        typeProperty = SimpleStringProperty(type)
    }
}

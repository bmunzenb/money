package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

sealed class FXCategory(name: String?, type: String? = null) {

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(name)
    val typeProperty: ReadOnlyStringProperty = SimpleStringProperty(type)

    data class Value(val category: Category) : FXCategory(
            name = category.name,
            type = category.type?.name
    )

    object Root : FXCategory(
            name = "Categories"
    )
}

package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

data class CategoryModel(
        var parent: Long? = null,
        var name: String? = null
) : Model()

object CategoryTable : Table<CategoryModel>() {

    override val name = "CATEGORIES"
    override val identityColumn = "CATEGORY_ID"

    const val parentColumn = "CATEGORY_PARENT"
    const val nameColumn = "CATEGORY_NAME"

    override fun setValues(settable: SettableQueryBuilder<*>, model: CategoryModel) {
        settable.set(parentColumn, model.parent)
        settable.set(nameColumn, model.name)
    }

    override fun getValues(resultSet: ResultSet, model: CategoryModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.parent = resultSet.getLongOrNull(parentColumn)
        model.name = resultSet.getString(nameColumn)
    }
}

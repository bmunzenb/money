package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

enum class CategoryType {
    INCOME, EXPENSE
}

data class CategoryModel(
        var name: String? = null,
        var parent: Long? = null,
        var type: CategoryType? = null
) : Model()

object CategoryTable : Table<CategoryModel>() {

    override val tableName = "CATEGORIES"
    override val identityColumn = "CATEGORY_ID"

    const val nameColumn = "CATEGORY_NAME"
    const val parentColumn = "CATEGORY_PARENT_ID"
    const val typeColumn = "CATEGORY_TYPE"

    override fun setValues(settable: SettableQueryBuilder<*>, model: CategoryModel) {
        settable.set(nameColumn, model.name)
        settable.set(parentColumn, model.parent)
        settable.set(typeColumn, model.type?.name)
    }

    override fun getValues(resultSet: ResultSet, model: CategoryModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.name = resultSet.getString(nameColumn)
        model.parent = resultSet.getLongOrNull(parentColumn)
        model.type = resultSet.getString(typeColumn)?.let { CategoryType.valueOf(it) }
    }
}

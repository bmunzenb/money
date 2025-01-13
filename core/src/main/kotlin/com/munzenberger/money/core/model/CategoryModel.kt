package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

enum class CategoryType {
    INCOME,
    EXPENSE,
}

data class CategoryModel(
    var name: String? = null,
    var parent: Long? = null,
    var type: CategoryType? = null,
) : Model()

object CategoryTable : Table<CategoryModel>() {
    override val tableName = "CATEGORIES"
    override val identityColumn = "CATEGORY_ID"

    const val CATEGORY_NAME = "CATEGORY_NAME"
    const val CATEGORY_PARENT_ID = "CATEGORY_PARENT_ID"
    const val CATEGORY_TYPE = "CATEGORY_TYPE"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: CategoryModel,
    ) {
        settable.set(CATEGORY_NAME, model.name)
        settable.set(CATEGORY_PARENT_ID, model.parent)
        settable.set(CATEGORY_TYPE, model.type?.name)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: CategoryModel,
    ): CategoryModel {
        return model.apply {
            identity = resultSet.getLong(identityColumn)
            name = resultSet.getString(CATEGORY_NAME)
            parent = resultSet.getLongOrNull(CATEGORY_PARENT_ID)
            type = resultSet.getString(CATEGORY_TYPE)?.let { CategoryType.valueOf(it) }
        }
    }
}

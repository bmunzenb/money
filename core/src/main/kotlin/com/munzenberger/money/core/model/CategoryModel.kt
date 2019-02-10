package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder

data class CategoryModel(var account: Long? = null, var name: String? = null) : Model()

object CategoryModelQueryBuilder : ModelQueryBuilder<CategoryModel>() {

    override val table = "CATEGORIES"
    override val identityColumn = "CATEGORY_ID"

    const val accountColumn = "CATEGORY_ACCOUNT_ID"
    const val nameColumn = "CATEGORY_NAME"

    override fun setValues(settable: SettableQueryBuilder<*>, model: CategoryModel) {
        settable.set(accountColumn, model.account)
        settable.set(nameColumn, model.name)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(accountColumn, AccountModelQueryBuilder)
    }
}

package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder

data class AccountTypeModel(var name: String? = null, var category: String? = null) : Model()

object AccountTypeQueryBuilder : ModelQueryBuilder<AccountTypeModel>() {

    override val table = "ACCOUNT_TYPES"
    override val identityColumn = "ACCOUNT_TYPE_ID"

    const val nameColumn = "ACCOUNT_TYPE_NAME"
    const val categoryColumn = "ACCOUNT_TYPE_CATEGORY"

    override fun setValues(settable: SettableQueryBuilder<*>, model: AccountTypeModel) {
        settable.set(nameColumn, model.name)
        settable.set(categoryColumn, model.category)
    }
}

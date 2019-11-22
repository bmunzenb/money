package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder

data class AccountTypeModel(
        var category: String? = null,
        var variant: String? = null
) : Model()

object AccountTypeTable : Table<AccountTypeModel>() {

    override val name = "ACCOUNT_TYPES"
    override val identityColumn = "ACCOUNT_TYPE_ID"

    const val categoryColumn = "ACCOUNT_TYPE_CATEGORY"
    const val variantColumn = "ACCOUNT_TYPE_VARIANT"

    override fun setValues(settable: SettableQueryBuilder<*>, model: AccountTypeModel) {
        settable.set(categoryColumn, model.category)
        settable.set(variantColumn, model.variant)
    }
}

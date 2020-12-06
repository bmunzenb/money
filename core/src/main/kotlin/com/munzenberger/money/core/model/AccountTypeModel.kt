package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder

data class AccountTypeModel(
        var group: String? = null,
        var variant: String? = null,
        var isCategory: Boolean? = null
) : Model()

object AccountTypeTable : Table<AccountTypeModel>() {

    override val name = "ACCOUNT_TYPES"
    override val identityColumn = "ACCOUNT_TYPE_ID"

    const val groupColumn = "ACCOUNT_TYPE_GROUP"
    const val variantColumn = "ACCOUNT_TYPE_VARIANT"
    const val isCategoryColumn = "ACCOUNT_TYPE_IS_CATEGORY"

    override fun setValues(settable: SettableQueryBuilder<*>, model: AccountTypeModel) {
        settable.set(groupColumn, model.group)
        settable.set(variantColumn, model.variant)
        settable.set(isCategoryColumn, model.isCategory)
    }
}

package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import java.sql.ResultSet

enum class AccountTypeGroup {
    ASSETS, LIABILITIES
}

enum class AccountTypeVariant {
    SAVINGS, CHECKING, ASSET, CASH, CREDIT, LOAN
}

data class AccountTypeModel(
        var group: AccountTypeGroup? = null,
        var variant: AccountTypeVariant? = null
) : Model()

object AccountTypeTable : Table<AccountTypeModel>() {

    override val tableName = "ACCOUNT_TYPES"
    override val identityColumn = "ACCOUNT_TYPE_ID"

    const val groupColumn = "ACCOUNT_TYPE_GROUP"
    const val variantColumn = "ACCOUNT_TYPE_VARIANT"

    override fun setValues(settable: SettableQueryBuilder<*>, model: AccountTypeModel) {
        settable.set(groupColumn, model.group?.name)
        settable.set(variantColumn, model.variant?.name)
    }

    override fun getValues(resultSet: ResultSet, model: AccountTypeModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.group = resultSet.getString(groupColumn)?.let { AccountTypeGroup.valueOf(it) }
        model.variant = resultSet.getString(variantColumn)?.let { AccountTypeVariant.valueOf(it) }
    }
}

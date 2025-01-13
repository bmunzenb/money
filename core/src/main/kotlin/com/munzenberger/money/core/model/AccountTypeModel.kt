package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import java.sql.ResultSet

enum class AccountTypeGroup {
    ASSETS,
    LIABILITIES,
}

enum class AccountTypeVariant {
    SAVINGS,
    CHECKING,
    ASSET,
    CASH,
    CREDIT,
    LOAN,
}

data class AccountTypeModel(
    var group: AccountTypeGroup? = null,
    var variant: AccountTypeVariant? = null,
) : Model()

object AccountTypeTable : Table<AccountTypeModel>() {
    override val tableName = "ACCOUNT_TYPES"
    override val identityColumn = "ACCOUNT_TYPE_ID"

    const val ACCOUNT_TYPE_GROUP = "ACCOUNT_TYPE_GROUP"
    const val ACCOUNT_TYPE_VARIANT = "ACCOUNT_TYPE_VARIANT"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: AccountTypeModel,
    ) {
        settable.set(ACCOUNT_TYPE_GROUP, model.group?.name)
        settable.set(ACCOUNT_TYPE_VARIANT, model.variant?.name)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: AccountTypeModel,
    ): AccountTypeModel =
        model.apply {
            identity = resultSet.getLong(identityColumn)
            group = resultSet.getString(ACCOUNT_TYPE_GROUP)?.let { AccountTypeGroup.valueOf(it) }
            variant = resultSet.getString(ACCOUNT_TYPE_VARIANT)?.let { AccountTypeVariant.valueOf(it) }
        }
}

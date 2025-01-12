package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

data class AccountModel(
    var name: String? = null,
    var number: String? = null,
    var accountType: Long? = null,
    var bank: Long? = null,
    var initialBalance: Long? = null,
) : Model()

object AccountTable : Table<AccountModel>() {
    override val tableName = "ACCOUNTS"
    override val identityColumn = "ACCOUNT_ID"

    const val ACCOUNT_NAME = "ACCOUNT_NAME"
    const val ACCOUNT_NUMBER = "ACCOUNT_NUMBER"
    const val ACCOUNT_TYPE_ID = "ACCOUNT_TYPE_ID"
    const val ACCOUNT_BANK_ID = "ACCOUNT_BANK_ID"
    const val ACCOUNT_INITIAL_BALANCE = "ACCOUNT_INITIAL_BALANCE"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: AccountModel,
    ) {
        settable.set(ACCOUNT_NAME, model.name)
        settable.set(ACCOUNT_NUMBER, model.number)
        settable.set(ACCOUNT_TYPE_ID, model.accountType)
        settable.set(ACCOUNT_BANK_ID, model.bank)
        settable.set(ACCOUNT_INITIAL_BALANCE, model.initialBalance)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: AccountModel,
    ) {
        model.identity = resultSet.getLong(identityColumn)
        model.name = resultSet.getString(ACCOUNT_NAME)
        model.number = resultSet.getString(ACCOUNT_NUMBER)
        model.accountType = resultSet.getLongOrNull(ACCOUNT_TYPE_ID)
        model.bank = resultSet.getLongOrNull(ACCOUNT_BANK_ID)
        model.initialBalance = resultSet.getLongOrNull(ACCOUNT_INITIAL_BALANCE)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(ACCOUNT_TYPE_ID, AccountTypeTable).leftJoin(ACCOUNT_BANK_ID, BankTable)
    }
}

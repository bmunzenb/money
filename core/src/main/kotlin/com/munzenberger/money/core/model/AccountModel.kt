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
        var initialBalance: Long? = null
) : Model()

object AccountTable : Table<AccountModel>() {

    override val name = "ACCOUNTS"
    override val identityColumn = "ACCOUNT_ID"

    const val nameColumn = "ACCOUNT_NAME"
    const val numberColumn = "ACCOUNT_NUMBER"
    const val accountTypeColumn = "ACCOUNT_TYPE_ID"
    const val bankColumn = "ACCOUNT_BANK_ID"
    const val initialBalanceColumn = "ACCOUNT_INITIAL_BALANCE"

    override fun setValues(settable: SettableQueryBuilder<*>, model: AccountModel) {
        settable.set(nameColumn, model.name)
        settable.set(numberColumn, model.number)
        settable.set(accountTypeColumn, model.accountType)
        settable.set(bankColumn, model.bank)
        settable.set(initialBalanceColumn, model.initialBalance)
    }

    override fun getValues(resultSet: ResultSet, model: AccountModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.name = resultSet.getString(nameColumn)
        model.number = resultSet.getString(numberColumn)
        model.accountType = resultSet.getLongOrNull(accountTypeColumn)
        model.bank = resultSet.getLongOrNull(bankColumn)
        model.initialBalance = resultSet.getLongOrNull(initialBalanceColumn)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(accountTypeColumn, AccountTypeTable).leftJoin(bankColumn, BankTable)
    }
}

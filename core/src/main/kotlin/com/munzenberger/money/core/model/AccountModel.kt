package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder

data class AccountModel(
        var name: String? = null,
        var number: String? = null,
        var accountType: Long? = null,
        var bank: Long? = null
) : Model()

object AccountModelQueryBuilder : ModelQueryBuilder<AccountModel>() {

    override val table = "ACCOUNTS"
    override val identityColumn = "ACCOUNT_ID"

    const val nameColumn = "ACCOUNT_NAME"
    const val numberColumn = "ACCOUNT_NUMBER"
    const val accountTypeColumn = "ACCOUNT_TYPE_ID"
    const val bankColumn = "BANK_ID"

    override fun setValues(settable: SettableQueryBuilder<*>, model: AccountModel) {
        settable.set(nameColumn, model.name)
        settable.set(numberColumn, model.number)
        settable.set(accountTypeColumn, model.accountType)
        settable.set(bankColumn, model.bank)
    }

    override fun select() = super.select().withJoins()

    override fun select(identity: Long) = super.select(identity).withJoins()

    private fun SelectQueryBuilder.withJoins() = this
            .leftJoin(AccountTypeModelQueryBuilder.table, accountTypeColumn, AccountTypeModelQueryBuilder.identityColumn)
            .leftJoin(BankModelQueryBuilder.table, bankColumn, BankModelQueryBuilder.identityColumn)
}

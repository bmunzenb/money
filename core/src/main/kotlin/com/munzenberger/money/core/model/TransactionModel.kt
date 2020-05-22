package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder

data class TransactionModel(
        var account: Long? = null,
        var payee: Long? = null,
        var date: Long? = null,
        var number: String? = null,
        var memo: String? = null,
        var status: String? = null
) : Model()

object TransactionTable : Table<TransactionModel>() {

    override val name = "TRANSACTIONS"
    override val identityColumn = "TRANSACTION_ID"

    const val accountColumn = "TRANSACTION_ACCOUNT_ID"
    const val payeeColumn = "TRANSACTION_PAYEE_ID"
    const val dateColumn = "TRANSACTION_DATE"
    const val numberColumn = "TRANSACTION_NUMBER"
    const val memoColumn = "TRANSACTION_MEMO"
    const val statusColumn = "TRANSACTION_STATUS"

    override fun setValues(settable: SettableQueryBuilder<*>, model: TransactionModel) {
        settable.set(accountColumn, model.account)
        settable.set(payeeColumn, model.payee)
        settable.set(dateColumn, model.date)
        settable.set(numberColumn, model.number)
        settable.set(memoColumn, model.memo)
        settable.set(statusColumn, model.status)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(accountColumn, AccountTable).leftJoin(payeeColumn, PayeeTable)
    }
}

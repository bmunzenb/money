package com.munzenberger.money.core.model

import com.munzenberger.money.sql.Condition
import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder

data class TransactionModel(
        var account: Long? = null,
        var payee: Long? = null,
        var date: Long? = null,
        var memo: String? = null
) : Model()

object TransactionModelQueryBuilder : ModelQueryBuilder<TransactionModel>() {

    override val table = "TRANSACTIONS"
    override val identityColumn = "TRANSACTION_ID"

    const val accountColumn = "TRANSACTION_ACCOUNT_ID"
    const val payeeColumn = "TRANSACTION_PAYEE_ID"
    const val dateColumn = "TRANSACTION_DATE"
    const val memoColumn = "TRANSACTION_MEMO"

    override fun setValues(settable: SettableQueryBuilder<*>, model: TransactionModel) {
        settable.set(accountColumn, model.account)
        settable.set(payeeColumn, model.payee)
        settable.set(dateColumn, model.date)
        settable.set(memoColumn, model.memo)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(accountColumn, AccountModelQueryBuilder).leftJoin(payeeColumn, PayeeModelQueryBuilder)
    }
}

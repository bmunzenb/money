package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder

data class TransferModel(
        var transaction: Long? = null,
        var account: Long? = null,
        var amount: Long? = null,
        var number: String? = null,
        var memo: String? = null,
        var status: String? = null
) : Model()

object TransferTable : Table<TransferModel>() {

    override val name = "TRANSFERS"
    override val identityColumn = "TRANSFER_ID"

    const val transactionColumn = "TRANSFER_TRANSACTION_ID"
    const val accountColumn = "TRANSFER_ACCOUNT_ID"
    const val amountColumn = "TRANSFER_AMOUNT"
    const val numberColumn = "TRANSFER_NUMBER"
    const val memoColumn = "TRANSFER_MEMO"
    const val statusColumn = "TRANSFER_STATUS"

    override fun setValues(settable: SettableQueryBuilder<*>, model: TransferModel) {
        settable.set(transactionColumn, model.transaction)
        settable.set(accountColumn, model.account)
        settable.set(amountColumn, model.amount)
        settable.set(numberColumn, model.number)
        settable.set(memoColumn, model.memo)
        settable.set(statusColumn, model.status)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(accountColumn, AccountTable)
    }
}

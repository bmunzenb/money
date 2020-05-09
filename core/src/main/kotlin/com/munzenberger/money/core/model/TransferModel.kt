package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder

data class TransferModel(
        var transaction: Long? = null,
        var category: Long? = null,
        var amount: Long? = null,
        var number: String? = null,
        var memo: String? = null
) : Model()

object TransferTable : Table<TransferModel>() {

    override val name = "TRANSFERS"
    override val identityColumn = "TRANSFER_ID"

    const val transactionColumn = "TRANSFER_TRANSACTION_ID"
    const val categoryColumn = "TRANSFER_CATEGORY_ID"
    const val amountColumn = "TRANSFER_AMOUNT"
    const val numberColumn = "TRANSFER_NUMBER"
    const val memoColumn = "TRANSFER_MEMO"

    override fun setValues(settable: SettableQueryBuilder<*>, model: TransferModel) {
        settable.set(transactionColumn, model.transaction)
        settable.set(categoryColumn, model.category)
        settable.set(amountColumn, model.amount)
        settable.set(numberColumn, model.number)
        settable.set(memoColumn, model.memo)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(categoryColumn, CategoryTable)
    }
}
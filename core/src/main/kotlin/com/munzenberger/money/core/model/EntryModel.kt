package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

data class EntryModel(
        var transaction: Long? = null,
        var category: Long? = null,
        var amount: Long? = null,
        var memo: String? = null
) : Model()

object EntryTable : Table<EntryModel>() {

    override val name = "ENTRIES"
    override val identityColumn = "ENTRY_ID"

    const val transactionColumn = "ENTRY_TRANSACTION_ID"
    const val categoryColumn = "ENTRY_CATEGORY_ID"
    const val amountColumn = "ENTRY_AMOUNT"
    const val memoColumn = "ENTRY_MEMO"

    override fun setValues(settable: SettableQueryBuilder<*>, model: EntryModel) {
        settable.set(transactionColumn, model.transaction)
        settable.set(categoryColumn, model.category)
        settable.set(amountColumn, model.amount)
        settable.set(memoColumn, model.memo)
    }

    override fun getValues(resultSet: ResultSet, model: EntryModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.transaction = resultSet.getLongOrNull(transactionColumn)
        model.category = resultSet.getLongOrNull(categoryColumn)
        model.amount = resultSet.getLongOrNull(amountColumn)
        model.memo = resultSet.getString(memoColumn)
    }
}

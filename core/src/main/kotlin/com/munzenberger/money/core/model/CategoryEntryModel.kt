package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

data class CategoryEntryModel(
        var transaction: Long? = null,
        var category: Long? = null,
        var amount: Long? = null,
        var memo: String? = null,
        var orderInTransaction: Int? = null
) : Model()

object CategoryEntryTable : Table<CategoryEntryModel>() {

    override val name = "CATEGORY_ENTRIES"
    override val identityColumn = "CATEGORY_ENTRY_ID"

    const val transactionColumn = "CATEGORY_ENTRY_TRANSACTION_ID"
    const val categoryColumn = "CATEGORY_ENTRY_CATEGORY_ID"
    const val amountColumn = "CATEGORY_ENTRY_AMOUNT"
    const val memoColumn = "CATEGORY_ENTRY_MEMO"
    const val orderInTransaction = "CATEGORY_ENTRY_ORDER_IN_TRANSACTION"

    override fun setValues(settable: SettableQueryBuilder<*>, model: CategoryEntryModel) {
        settable.set(transactionColumn, model.transaction)
        settable.set(categoryColumn, model.category)
        settable.set(amountColumn, model.amount)
        settable.set(memoColumn, model.memo)
        settable.set(orderInTransaction, model.orderInTransaction)
    }

    override fun getValues(resultSet: ResultSet, model: CategoryEntryModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.transaction = resultSet.getLongOrNull(transactionColumn)
        model.category = resultSet.getLongOrNull(categoryColumn)
        model.amount = resultSet.getLongOrNull(amountColumn)
        model.memo = resultSet.getString(memoColumn)
        model.orderInTransaction = resultSet.getInt(orderInTransaction)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(categoryColumn, CategoryTable)
    }
}

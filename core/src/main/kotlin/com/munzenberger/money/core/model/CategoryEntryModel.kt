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
    var orderInTransaction: Int? = null,
) : Model()

object CategoryEntryTable : Table<CategoryEntryModel>() {
    override val tableName = "CATEGORY_ENTRIES"
    override val identityColumn = "CATEGORY_ENTRY_ID"

    const val CATEGORY_ENTRY_TRANSACTION_ID = "CATEGORY_ENTRY_TRANSACTION_ID"
    const val CATEGORY_ENTRY_CATEGORY_ID = "CATEGORY_ENTRY_CATEGORY_ID"
    const val CATEGORY_ENTRY_AMOUNT = "CATEGORY_ENTRY_AMOUNT"
    const val CATEGORY_ENTRY_MEMO = "CATEGORY_ENTRY_MEMO"
    const val CATEGORY_ENTRY_ORDER_IN_TRANSACTION = "CATEGORY_ENTRY_ORDER_IN_TRANSACTION"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: CategoryEntryModel,
    ) {
        settable.set(CATEGORY_ENTRY_TRANSACTION_ID, model.transaction)
        settable.set(CATEGORY_ENTRY_CATEGORY_ID, model.category)
        settable.set(CATEGORY_ENTRY_AMOUNT, model.amount)
        settable.set(CATEGORY_ENTRY_MEMO, model.memo)
        settable.set(CATEGORY_ENTRY_ORDER_IN_TRANSACTION, model.orderInTransaction)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: CategoryEntryModel,
    ): CategoryEntryModel {
        return model.apply {
            identity = resultSet.getLong(identityColumn)
            transaction = resultSet.getLongOrNull(CATEGORY_ENTRY_TRANSACTION_ID)
            category = resultSet.getLongOrNull(CATEGORY_ENTRY_CATEGORY_ID)
            amount = resultSet.getLongOrNull(CATEGORY_ENTRY_AMOUNT)
            memo = resultSet.getString(CATEGORY_ENTRY_MEMO)
            orderInTransaction = resultSet.getInt(CATEGORY_ENTRY_ORDER_IN_TRANSACTION)
        }
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(CATEGORY_ENTRY_CATEGORY_ID, CategoryTable)
    }
}

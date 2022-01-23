package com.munzenberger.money.core.model

import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

data class TransferEntryModel(
        var transaction: Long? = null,
        var account: Long? = null,
        var amount: Long? = null,
        var number: String? = null,
        var memo: String? = null,
        var status: TransactionStatus? = null,
        var orderInTransaction: Int? = null
) : Model()

object TransferEntryTable : Table<TransferEntryModel>() {

    override val tableName = "TRANSFER_ENTRIES"
    override val identityColumn = "TRANSFER_ENTRY_ID"

    const val transactionColumn = "TRANSFER_ENTRY_TRANSACTION_ID"
    const val accountColumn = "TRANSFER_ENTRY_ACCOUNT_ID"
    const val amountColumn = "TRANSFER_ENTRY_AMOUNT"
    const val numberColumn = "TRANSFER_ENTRY_NUMBER"
    const val memoColumn = "TRANSFER_ENTRY_MEMO"
    const val statusColumn = "TRANSFER_ENTRY_STATUS"
    const val orderInTransaction = "TRANSFER_ENTRY_ORDER_IN_TRANSACTION"

    override fun setValues(settable: SettableQueryBuilder<*>, model: TransferEntryModel) {
        settable.set(transactionColumn, model.transaction)
        settable.set(accountColumn, model.account)
        settable.set(amountColumn, model.amount)
        settable.set(numberColumn, model.number)
        settable.set(memoColumn, model.memo)
        settable.set(statusColumn, model.status?.name)
        settable.set(orderInTransaction, model.orderInTransaction)
    }

    override fun getValues(resultSet: ResultSet, model: TransferEntryModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.transaction = resultSet.getLongOrNull(transactionColumn)
        model.account = resultSet.getLongOrNull(accountColumn)
        model.amount = resultSet.getLongOrNull(amountColumn)
        model.number = resultSet.getString(numberColumn)
        model.memo = resultSet.getString(memoColumn)
        model.status = resultSet.getString(statusColumn)?.let { TransactionStatus.valueOf(it) }
        model.orderInTransaction = resultSet.getInt(orderInTransaction)
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(accountColumn, AccountTable)
    }
}

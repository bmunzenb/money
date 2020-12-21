package com.munzenberger.money.core.model

import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

data class TransferModel(
        var transaction: Long? = null,
        var account: Long? = null,
        var amount: Long? = null,
        var number: String? = null,
        var memo: String? = null,
        var status: TransactionStatus? = null,
        var orderInTransaction: Int? = null
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
    const val orderInTransaction = "TRANSFER_ORDER_IN_TRANSACTION"

    override fun setValues(settable: SettableQueryBuilder<*>, model: TransferModel) {
        settable.set(transactionColumn, model.transaction)
        settable.set(accountColumn, model.account)
        settable.set(amountColumn, model.amount)
        settable.set(numberColumn, model.number)
        settable.set(memoColumn, model.memo)
        settable.set(statusColumn, model.status?.name)
        settable.set(orderInTransaction, model.orderInTransaction)
    }

    override fun getValues(resultSet: ResultSet, model: TransferModel) {
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

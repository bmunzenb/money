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
    var orderInTransaction: Int? = null,
) : Model()

object TransferEntryTable : Table<TransferEntryModel>() {
    override val tableName = "TRANSFER_ENTRIES"
    override val identityColumn = "TRANSFER_ENTRY_ID"

    const val TRANSFER_ENTRY_TRANSACTION_ID = "TRANSFER_ENTRY_TRANSACTION_ID"
    const val TRANSFER_ENTRY_ACCOUNT_ID = "TRANSFER_ENTRY_ACCOUNT_ID"
    const val TRANSFER_ENTRY_AMOUNT = "TRANSFER_ENTRY_AMOUNT"
    const val TRANSFER_ENTRY_NUMBER = "TRANSFER_ENTRY_NUMBER"
    const val TRANSFER_ENTRY_MEMO = "TRANSFER_ENTRY_MEMO"
    const val TRANSFER_ENTRY_STATUS = "TRANSFER_ENTRY_STATUS"
    const val TRANSFER_ENTRY_ORDER_IN_TRANSACTION = "TRANSFER_ENTRY_ORDER_IN_TRANSACTION"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: TransferEntryModel,
    ) {
        settable.set(TRANSFER_ENTRY_TRANSACTION_ID, model.transaction)
        settable.set(TRANSFER_ENTRY_ACCOUNT_ID, model.account)
        settable.set(TRANSFER_ENTRY_AMOUNT, model.amount)
        settable.set(TRANSFER_ENTRY_NUMBER, model.number)
        settable.set(TRANSFER_ENTRY_MEMO, model.memo)
        settable.set(TRANSFER_ENTRY_STATUS, model.status?.name)
        settable.set(TRANSFER_ENTRY_ORDER_IN_TRANSACTION, model.orderInTransaction)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: TransferEntryModel,
    ): TransferEntryModel =
        model.apply {
            identity = resultSet.getLong(identityColumn)
            transaction = resultSet.getLongOrNull(TRANSFER_ENTRY_TRANSACTION_ID)
            account = resultSet.getLongOrNull(TRANSFER_ENTRY_ACCOUNT_ID)
            amount = resultSet.getLongOrNull(TRANSFER_ENTRY_AMOUNT)
            number = resultSet.getString(TRANSFER_ENTRY_NUMBER)
            memo = resultSet.getString(TRANSFER_ENTRY_MEMO)
            status = resultSet.getString(TRANSFER_ENTRY_STATUS)?.let { TransactionStatus.valueOf(it) }
            orderInTransaction = resultSet.getInt(TRANSFER_ENTRY_ORDER_IN_TRANSACTION)
        }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(TRANSFER_ENTRY_ACCOUNT_ID, AccountTable)
    }
}

package com.munzenberger.money.core.model

import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet

data class TransactionModel(
    var account: Long? = null,
    var payee: Long? = null,
    var date: Long? = null,
    var number: String? = null,
    var memo: String? = null,
    var status: TransactionStatus? = null,
) : Model()

object TransactionTable : Table<TransactionModel>() {
    override val tableName = "TRANSACTIONS"
    override val identityColumn = "TRANSACTION_ID"

    const val TRANSACTION_ACCOUNT_ID = "TRANSACTION_ACCOUNT_ID"
    const val TRANSACTION_PAYEE_ID = "TRANSACTION_PAYEE_ID"
    const val TRANSACTION_DATE = "TRANSACTION_DATE"
    const val TRANSACTION_NUMBER = "TRANSACTION_NUMBER"
    const val TRANSACTION_MEMO = "TRANSACTION_MEMO"
    const val TRANSACTION_STATUS = "TRANSACTION_STATUS"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: TransactionModel,
    ) {
        settable.set(TRANSACTION_ACCOUNT_ID, model.account)
        settable.set(TRANSACTION_PAYEE_ID, model.payee)
        settable.set(TRANSACTION_DATE, model.date)
        settable.set(TRANSACTION_NUMBER, model.number)
        settable.set(TRANSACTION_MEMO, model.memo)
        settable.set(TRANSACTION_STATUS, model.status?.name)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: TransactionModel,
    ): TransactionModel {
        return model.apply {
            identity = resultSet.getLong(identityColumn)
            account = resultSet.getLongOrNull(TRANSACTION_ACCOUNT_ID)
            payee = resultSet.getLongOrNull(TRANSACTION_PAYEE_ID)
            date = resultSet.getLongOrNull(TRANSACTION_DATE)
            number = resultSet.getString(TRANSACTION_NUMBER)
            memo = resultSet.getString(TRANSACTION_MEMO)
            status = resultSet.getString(TRANSACTION_STATUS)?.let { TransactionStatus.valueOf(it) }
        }
    }

    override fun applyJoins(select: SelectQueryBuilder) {
        select.leftJoin(TRANSACTION_ACCOUNT_ID, AccountTable).leftJoin(TRANSACTION_PAYEE_ID, PayeeTable)
    }
}

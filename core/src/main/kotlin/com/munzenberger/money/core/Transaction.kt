package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionModel
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.getLongOrNull
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet
import java.time.LocalDate

class Transaction internal constructor(model: TransactionModel) : Persistable<TransactionModel>(model, TransactionTable) {

    constructor() : this(TransactionModel(
            status = TransactionStatus.UNRECONCILED.name
    ))

    var date: LocalDate?
        get() = model.date?.let { LocalDate.ofEpochDay(it) }
        set(value) { model.date = value?.toEpochDay() }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var number: String?
        get() = model.number
        set(value) { model.number = value }

    var account: Account? = null

    var payee: Payee? = null

    var status: TransactionStatus
        get() = TransactionStatus.parse(model.status)
        set(value) { model.status = value.name }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.account = account.getIdentity(tx)
        model.payee = payee.getIdentity(tx)
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, TransactionTable, TransactionResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, TransactionTable, TransactionResultSetMapper())
    }
}

class TransactionResultSetMapper : ResultSetMapper<Transaction> {

    override fun apply(resultSet: ResultSet): Transaction {

        val model = TransactionModel().apply {
            identity = resultSet.getLong(TransactionTable.identityColumn)
            account = resultSet.getLongOrNull(TransactionTable.accountColumn)
            payee = resultSet.getLongOrNull(TransactionTable.payeeColumn)
            date = resultSet.getLongOrNull(TransactionTable.dateColumn)
            number = resultSet.getString(TransactionTable.numberColumn)
            memo = resultSet.getString(TransactionTable.memoColumn)
            status = resultSet.getString(TransactionTable.statusColumn)
        }

        return Transaction(model).apply {
            account = model.account?.let { AccountResultSetMapper().apply(resultSet) }
            payee = model.payee?.let { PayeeResultSetMapper().apply(resultSet) }
        }
    }
}

fun Transaction.getTransfers(database: MoneyDatabase): List<Transfer> =
        when (val id = identity) {
            null ->
                emptyList()

            else -> {
                val query = TransferTable.select()
                        .where(TransferTable.transactionColumn.eq(id))
                        .orderBy(TransferTable.identityColumn)
                        .build()

                database.getList(query, TransferResultSetMapper())
            }
        }

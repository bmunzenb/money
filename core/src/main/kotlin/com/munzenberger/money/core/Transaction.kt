package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.TransactionModel
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferEntryTable
import com.munzenberger.money.sql.OrderableQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet
import java.time.LocalDate

data class TransactionIdentity(override val value: Long) : Identity

class Transaction internal constructor(model: TransactionModel) : AbstractMoneyEntity<TransactionIdentity, TransactionModel>(
    model,
    TransactionTable,
) {
    constructor() : this(
        TransactionModel(
            status = TransactionStatus.UNRECONCILED,
        ),
    )

    override val identity: TransactionIdentity?
        get() = model.identity?.let { TransactionIdentity(it) }

    var date: LocalDate?
        get() = model.date?.let { LocalDate.ofEpochDay(it) }
        set(value) {
            model.date = value?.toEpochDay()
        }

    var memo: String?
        get() = model.memo
        set(value) {
            model.memo = value
        }

    var number: String?
        get() = model.number
        set(value) {
            model.number = value
        }

    var account: Account? = null

    var payee: Payee? = null

    var status: TransactionStatus?
        get() = model.status
        set(value) {
            model.status = value
        }

    override fun save(executor: QueryExecutor) =
        executor.transaction { tx ->
            model.account = account?.getAutoSavedIdentity(tx)?.value
            model.payee = payee?.getAutoSavedIdentity(tx)?.value
            super.save(tx)
        }

    companion object {
        fun find(
            executor: QueryExecutor,
            block: OrderableQueryBuilder<*>.() -> Unit = {},
        ) = MoneyEntity.find(executor, TransactionTable, TransactionResultSetMapper, block)

        fun get(
            identity: TransactionIdentity,
            executor: QueryExecutor,
        ) = MoneyEntity.get(identity, executor, TransactionTable, TransactionResultSetMapper)
    }
}

object TransactionResultSetMapper : ResultSetMapper<Transaction> {
    override fun apply(resultSet: ResultSet): Transaction {
        val model =
            TransactionModel().apply {
                TransactionTable.getValues(resultSet, this)
            }

        return Transaction(model).apply {
            account = model.account?.let { AccountResultSetMapper.apply(resultSet) }
            payee = model.payee?.let { PayeeResultSetMapper.apply(resultSet) }
        }
    }
}

fun Transaction.getEntries(executor: QueryExecutor): List<Entry<out EntryIdentity>> {
    val transfers =
        TransferEntryTable.select {
            where(TransferEntryTable.TRANSFER_ENTRY_TRANSACTION_ID.eq(identity?.value))
        }.let { executor.getList(it, TransferEntryResultSetMapper) }

    val categories =
        CategoryEntryTable.select {
            where(CategoryEntryTable.CATEGORY_ENTRY_TRANSACTION_ID.eq(identity?.value))
        }.let { executor.getList(it, CategoryEntryResultSetMapper) }

    return (transfers + categories).sortedBy { it.orderInTransaction }
}

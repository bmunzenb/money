package com.munzenberger.money.app.model

import com.munzenberger.money.app.SchedulerProvider
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.DeleteQueryBuilder
import com.munzenberger.money.sql.inGroup
import com.munzenberger.money.sql.transaction
import io.reactivex.Single
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.util.Date

class FXAccountTransaction(accountTransaction: AccountTransaction) {

    internal val transactionId = accountTransaction.transactionId

    val dateProperty: ReadOnlyObjectProperty<Date> = SimpleObjectProperty(accountTransaction.date)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountTransaction.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(accountTransaction.payee)

    val categoryProperty: ReadOnlyStringProperty
    val debitProperty: ReadOnlyObjectProperty<Money>
    val creditProperty: ReadOnlyObjectProperty<Money>

    init {

        categoryProperty = SimpleStringProperty().apply {
            value = when (accountTransaction.categories.size) {
                0 -> null
                1 -> accountTransaction.categories[0]
                else -> SPLIT_CATEGORY_NAME
            }
        }

        when {
            accountTransaction.amount.isNegative -> {
                debitProperty = SimpleObjectProperty(accountTransaction.amount.negate())
                creditProperty = SimpleObjectProperty()
            }
            else -> {
                debitProperty = SimpleObjectProperty()
                creditProperty = SimpleObjectProperty(accountTransaction.amount)
            }
        }
    }

    fun getTransaction(database: MoneyDatabase, block: (Transaction?, Throwable?) -> Unit) {
        Single.fromCallable {
            Transaction.get(transactionId, database)
                    ?: throw PersistableNotFoundException(Transaction::class, transactionId)
        }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe { transaction, error -> block.invoke(transaction, error) }
    }
}

fun List<FXAccountTransaction>.delete(database: MoneyDatabase, block: (Throwable?) -> Unit) {

    val ids = map { it.transactionId }

    Single.fromCallable {
        database.transaction { tx ->

            val deleteTransfers = DeleteQueryBuilder(TransferTable.name)
                    .where(TransferTable.transactionColumn.inGroup(ids))
                    .build()

            tx.executeUpdate(deleteTransfers)

            val deleteTransactions = DeleteQueryBuilder(TransactionTable.name)
                    .where(TransactionTable.identityColumn.inGroup(ids))
                    .build()

            tx.executeUpdate(deleteTransactions)
        }
    }
            .subscribeOn(SchedulerProvider.database)
            .observeOn(SchedulerProvider.main)
            .subscribe { _, error -> block.invoke(error) }
}

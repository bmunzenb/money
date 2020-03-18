package com.munzenberger.money.app.model

import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Observable
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.sql.ResultSet
import java.util.Date

class FXTransactionDetail(val identity: Long) {

    private val date = SimpleObjectProperty<Date>()
    private val payee = SimpleStringProperty()
    private val amount = SimpleObjectProperty(Money.zero())
    private val category = SimpleStringProperty()
    private val memo = SimpleStringProperty()
    private val payment = SimpleObjectProperty<Money>()
    private val deposit = SimpleObjectProperty<Money>()
    private val balance = SimpleObjectProperty<Money>()

    companion object {

        fun getTransactionsForAccount(accountId: Long, database: MoneyDatabase): List<FXTransactionDetail> {

            val list = mutableListOf<FXTransactionDetail>()

            val query = Query(
                    """SELECT TRANSACTION_ID, TRANSACTION_DATE, PAYEE_NAME, TRANSFER_AMOUNT, SOURCE_ACCOUNT.ACCOUNT_ID AS SOURCE_ACCOUNT_ID, SOURCE_ACCOUNT.ACCOUNT_NAME AS SOURCE_ACCOUNT_NAME, TARGET_ACCOUNT.ACCOUNT_ID AS TARGET_ACCOUNT_ID, TARGET_ACCOUNT.ACCOUNT_NAME AS TARGET_ACCOUNT_NAME, TRANSACTION_MEMO
                            FROM TRANSACTIONS
                            LEFT JOIN ACCOUNTS AS SOURCE_ACCOUNT ON TRANSACTIONS.TRANSACTION_ACCOUNT_ID = SOURCE_ACCOUNT.ACCOUNT_ID
                            LEFT JOIN PAYEES ON TRANSACTIONS.TRANSACTION_PAYEE_ID = PAYEES.PAYEE_ID
                            LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
                            LEFT JOIN CATEGORIES ON TRANSFERS.TRANSFER_CATEGORY_ID = CATEGORIES.CATEGORY_ID
                            LEFT JOIN ACCOUNTS AS TARGET_ACCOUNT ON CATEGORIES.CATEGORY_ACCOUNT_ID = TARGET_ACCOUNT.ACCOUNT_ID
                            WHERE TRANSACTION_ACCOUNT_ID = ? OR CATEGORY_ACCOUNT_ID = ?""",
                    listOf(accountId, accountId))

            database.executeQuery(query, object : ResultSetHandler {
                override fun accept(rs: ResultSet) {

                    var balance: Long = 0

                    var t: FXTransactionDetail? = null

                    while (rs.next()) {

                        val transactionId = rs.getLong("TRANSACTION_ID")
                        val date = rs.getDate("TRANSACTION_DATE")
                        val payee: String? = rs.getString("PAYEE_NAME")
                        val transferAmount = rs.getLong("TRANSFER_AMOUNT")
                        val sourceAccountId = rs.getLong("SOURCE_ACCOUNT_ID")
                        val sourceAccountName = rs.getString("SOURCE_ACCOUNT_NAME")
                        val targetAccountId = rs.getLongOrNull("TARGET_ACCOUNT_ID")
                        val targetAccountName = rs.getLongOrNull("TARGET_ACCOUNT_NAME")
                        val memo: String? = rs.getString("TRANSACTION_MEMO")

                        when {
                            t == null -> {
                                // first transaction
                                t = FXTransactionDetail(transactionId)
                            }
                            t.identity != transactionId -> {

                                // end of previous transaction
                                t.balance.value = Money.valueOf(balance)
                                list.add(t)

                                // start of new transaction
                                t = FXTransactionDetail(transactionId).apply {
                                    this.date.value = date
                                    this.payee.value = payee
                                    this.memo.value = memo
                                }
                            }
                        }

                        //
                    }

                    t?.run {
                        // last transaction
                        this.balance.value = Money.valueOf(balance)
                        list.add(t)
                    }
                }
            })

            return list
        }

        fun observableTransactionsForAccount(accountId: Long, database: ObservableMoneyDatabase): Observable<List<FXTransactionDetail>> =
                database.onUpdate.flatMap { Observable.fromCallable { getTransactionsForAccount(accountId, database) } }
    }
}

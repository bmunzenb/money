package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.getLongOrNull
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.sql.ResultSet
import java.util.Date

class FXTransactionDetail(val identity: Long) {

    val dateProperty = SimpleObjectProperty<Date>()
    val payeeProperty = SimpleStringProperty()
    val categoryProperty = SimpleStringProperty()
    val memoProperty = SimpleStringProperty()
    val paymentProperty = SimpleObjectProperty<Money>()
    val depositProperty = SimpleObjectProperty<Money>()
    val balanceProperty = SimpleObjectProperty<Money>()

    companion object {

        private const val SQL_QUERY =
                "SELECT TRANSACTION_ID, TRANSACTION_DATE, PAYEE_NAME, TRANSFER_AMOUNT, SOURCE_ACCOUNT.ACCOUNT_ID AS SOURCE_ACCOUNT_ID, SOURCE_ACCOUNT.ACCOUNT_NAME AS SOURCE_ACCOUNT_NAME, TARGET_ACCOUNT.ACCOUNT_ID AS TARGET_ACCOUNT_ID, TARGET_ACCOUNT.ACCOUNT_NAME AS TARGET_ACCOUNT_NAME, TRANSACTION_MEMO " + "FROM TRANSACTIONS " +
                "LEFT JOIN ACCOUNTS AS SOURCE_ACCOUNT ON TRANSACTIONS.TRANSACTION_ACCOUNT_ID = SOURCE_ACCOUNT.ACCOUNT_ID " +
                "LEFT JOIN PAYEES ON TRANSACTIONS.TRANSACTION_PAYEE_ID = PAYEES.PAYEE_ID " +
                "LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID " +
                "LEFT JOIN CATEGORIES ON TRANSFERS.TRANSFER_CATEGORY_ID = CATEGORIES.CATEGORY_ID " +
                "LEFT JOIN ACCOUNTS AS TARGET_ACCOUNT ON CATEGORIES.CATEGORY_ACCOUNT_ID = TARGET_ACCOUNT.ACCOUNT_ID " +
                "WHERE TRANSACTION_ACCOUNT_ID = ? OR CATEGORY_ACCOUNT_ID = ? " +
                "ORDER BY TRANSACTION_DATE ASC"

        fun getTransactionsForAccount(accountId: Long, initialBalance: Money, database: MoneyDatabase): List<FXTransactionDetail> {

            val list = mutableListOf<FXTransactionDetail>()

            val query = Query(SQL_QUERY, listOf(accountId, accountId))

            database.executeQuery(query, object : ResultSetHandler {
                override fun accept(rs: ResultSet) {

                    var balance: Long = initialBalance.value

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

                        t?.run {
                            // start new transaction
                            if (identity != transactionId) {
                                balanceProperty.value = Money.valueOf(balance)
                                list.add(this)
                                t = FXTransactionDetail(transactionId)
                            }
                        }

                        if (t == null) {
                            // first transaction
                            t = FXTransactionDetail(transactionId)
                        }

                        t?.run {
                            dateProperty.value = date
                            payeeProperty.value = payee
                            memoProperty.value = memo

                            // calculate category totals
                        }
                    }

                    t?.run {
                        // last transaction
                        balanceProperty.value = Money.valueOf(balance)
                        list.add(this)
                    }
                }
            })

            return list
        }
    }
}

fun Account.getTransactionDetails(database: MoneyDatabase): List<FXTransactionDetail> =
        FXTransactionDetail.getTransactionsForAccount(identity!!, initialBalance ?: Money.zero(), database)

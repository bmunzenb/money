package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.getLongOrNull
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.sql.ResultSet
import java.util.Date

class FXTransactionDetail private constructor(
        val identity: Long,
        date: Date,
        payee: String?,
        memo: String?,
        category: String?,
        amount: Money,
        balance: Money
) {

    val dateProperty: ReadOnlyObjectProperty<Date> = SimpleObjectProperty(date)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(payee)
    val categoryProperty: ReadOnlyStringProperty = SimpleStringProperty(category)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(balance)

    val paymentProperty: ReadOnlyObjectProperty<Money>
    val depositProperty: ReadOnlyObjectProperty<Money>

    init {
        when {
            amount.isNegative -> {
                paymentProperty = SimpleObjectProperty(amount.negate())
                depositProperty = SimpleObjectProperty()
            }
            else -> {
                paymentProperty = SimpleObjectProperty()
                depositProperty = SimpleObjectProperty(amount)
            }
        }
    }

    private class TransactionDetailCollector(private val accountId: Long, initialBalance: Money) {

        private var identity: Long? = null
        private var date: Date? = null
        private var payee: String? = null
        private var memo: String? = null
        private var category: String? = null
        private var amount: Long = 0
        private var balance: Long = initialBalance.value

        fun ready(transactionId: Long) =
                identity == null || transactionId == identity

        fun collect(
                transactionId: Long,
                date: Date,
                payee: String?,
                memo: String?,
                transferAmount: Long,
                sourceAccountId: Long,
                sourceAccountTypeCategory: String,
                sourceAccountName: String,
                targetAccountId: Long?,
                targetAccountTypeCategory: String?,
                targetAccountName: String?,
                categoryName: String?
        ) {
            this.identity = transactionId
            this.date = date
            this.payee = payee
            this.memo = memo

            // calculate the total amount for this transaction
            // this is not an else in case the source and target are the same

            if (accountId == sourceAccountId) {
                amount += transferAmount

                category = when (category) {
                    null -> categoryName(
                            accountTypeCategory = targetAccountTypeCategory?.let { AccountType.Category.valueOf(it) },
                            accountName = targetAccountName,
                            categoryName = categoryName
                    )
                    else -> SPLIT_CATEGORY_NAME
                }
            }

            if (accountId == targetAccountId) {
                amount -= transferAmount

                category = when (category) {
                    null -> categoryName(
                            accountTypeCategory = AccountType.Category.valueOf(sourceAccountTypeCategory),
                            accountName = sourceAccountName
                    )
                    else -> SPLIT_CATEGORY_NAME
                }
            }
        }

        fun next(): FXTransactionDetail? {
            balance += amount
            val t = when (identity) {
                null -> null
                else -> FXTransactionDetail(
                        identity = identity!!,
                        date = date!!,
                        payee = payee,
                        memo = memo,
                        category = category,
                        amount = Money.valueOf(amount),
                        balance = Money.valueOf(balance)
                )
            }
            reset()
            return t
        }

        private fun reset() {
            identity = null
            date = null
            payee = null
            memo = null
            category = null
            amount = 0
        }
    }

    companion object {

        fun getTransactionsForAccount(accountId: Long, initialBalance: Money, database: MoneyDatabase): List<FXTransactionDetail> {

            val list = mutableListOf<FXTransactionDetail>()

            val sql =
                    "SELECT TRANSACTION_ID, TRANSACTION_DATE, PAYEE_NAME, TRANSFER_AMOUNT, TRANSACTION_MEMO, CATEGORY_NAME, " +
                        "SOURCE_ACCOUNT.ACCOUNT_ID AS SOURCE_ACCOUNT_ID, SOURCE_ACCOUNT.ACCOUNT_NAME AS SOURCE_ACCOUNT_NAME, SOURCE_ACCOUNT_TYPE.ACCOUNT_TYPE_CATEGORY AS SOURCE_ACCOUNT_TYPE_CATEGORY, " +
                        "TARGET_ACCOUNT.ACCOUNT_ID AS TARGET_ACCOUNT_ID, TARGET_ACCOUNT.ACCOUNT_NAME AS TARGET_ACCOUNT_NAME, TARGET_ACCOUNT_TYPE.ACCOUNT_TYPE_CATEGORY AS TARGET_ACCOUNT_TYPE_CATEGORY " +
                    "FROM TRANSACTIONS " +
                    "LEFT JOIN ACCOUNTS AS SOURCE_ACCOUNT ON TRANSACTIONS.TRANSACTION_ACCOUNT_ID = SOURCE_ACCOUNT.ACCOUNT_ID " +
                    "LEFT JOIN ACCOUNT_TYPES AS SOURCE_ACCOUNT_TYPE ON SOURCE_ACCOUNT.ACCOUNT_TYPE_ID = SOURCE_ACCOUNT_TYPE.ACCOUNT_TYPE_ID " +
                    "LEFT JOIN PAYEES ON TRANSACTIONS.TRANSACTION_PAYEE_ID = PAYEES.PAYEE_ID " +
                    "LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID " +
                    "LEFT JOIN CATEGORIES ON TRANSFERS.TRANSFER_CATEGORY_ID = CATEGORIES.CATEGORY_ID " +
                    "LEFT JOIN ACCOUNTS AS TARGET_ACCOUNT ON CATEGORIES.CATEGORY_ACCOUNT_ID = TARGET_ACCOUNT.ACCOUNT_ID " +
                    "LEFT JOIN ACCOUNT_TYPES AS TARGET_ACCOUNT_TYPE ON TARGET_ACCOUNT.ACCOUNT_TYPE_ID = TARGET_ACCOUNT_TYPE.ACCOUNT_TYPE_ID " +
                    "WHERE TRANSACTION_ACCOUNT_ID = ? OR CATEGORY_ACCOUNT_ID = ? " +
                    "ORDER BY TRANSACTION_DATE ASC"

            val query = Query(sql, listOf(accountId, accountId))

            database.executeQuery(query, object : ResultSetHandler {
                override fun accept(rs: ResultSet) {

                    val collector = TransactionDetailCollector(accountId, initialBalance)

                    while (rs.next()) {

                        val transactionId = rs.getLong("TRANSACTION_ID")
                        val date = rs.getDate("TRANSACTION_DATE")
                        val payee: String? = rs.getString("PAYEE_NAME")
                        val transferAmount = rs.getLong("TRANSFER_AMOUNT")
                        val sourceAccountId = rs.getLong("SOURCE_ACCOUNT_ID")
                        val sourceAccountTypeCategory = rs.getString("SOURCE_ACCOUNT_TYPE_CATEGORY")
                        val sourceAccountName = rs.getString("SOURCE_ACCOUNT_NAME")
                        val targetAccountId = rs.getLongOrNull("TARGET_ACCOUNT_ID")
                        val targetAccountTypeCategory: String? = rs.getString("TARGET_ACCOUNT_TYPE_CATEGORY")
                        val targetAccountName: String? = rs.getString("TARGET_ACCOUNT_NAME")
                        val categoryName: String? = rs.getString("CATEGORY_NAME")
                        val memo: String? = rs.getString("TRANSACTION_MEMO")

                        if (!collector.ready(transactionId)) {
                            collector.next()?.let { list.add(it) }
                        }

                        collector.collect(
                                transactionId = transactionId,
                                date = date,
                                payee = payee,
                                transferAmount = transferAmount,
                                sourceAccountId = sourceAccountId,
                                sourceAccountTypeCategory = sourceAccountTypeCategory,
                                sourceAccountName = sourceAccountName,
                                targetAccountId = targetAccountId,
                                targetAccountTypeCategory = targetAccountTypeCategory,
                                targetAccountName = targetAccountName,
                                categoryName = categoryName,
                                memo = memo
                        )
                    }

                    collector.next()?.let { list.add(it) }
                }
            })

            return list
        }
    }
}

fun Account.getTransactionDetails(database: MoneyDatabase): List<FXTransactionDetail> =
        FXTransactionDetail.getTransactionsForAccount(identity!!, initialBalance ?: Money.zero(), database)

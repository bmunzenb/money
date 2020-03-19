package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.getLongOrNull
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.sql.ResultSet
import java.util.Date

class FXTransactionDetail private constructor(val identity: Long, date: Date, payee: String?, memo: String?) {

    private val category = SimpleStringProperty()
    private val amount = SimpleObjectProperty(Money.zero())
    private val balance = SimpleObjectProperty<Money>()

    val dateProperty: ReadOnlyObjectProperty<Date> = SimpleObjectProperty(date)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(payee)
    val memoProperty: ReadOnlyStringProperty = SimpleStringProperty(memo)
    val categoryProperty: ReadOnlyStringProperty = category
    val amountProperty: ReadOnlyObjectProperty<Money> = amount
    val balanceProperty: ReadOnlyObjectProperty<Money> = balance

    companion object {

        private const val SQL_QUERY =
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

        fun getTransactionsForAccount(accountId: Long, initialBalance: Money, database: MoneyDatabase): List<FXTransactionDetail> {

            val list = mutableListOf<FXTransactionDetail>()

            val query = Query(SQL_QUERY, listOf(accountId, accountId))

            database.executeQuery(query, object : ResultSetHandler {
                override fun accept(rs: ResultSet) {

                    var category: String? = null
                    var amount: Long = 0
                    var balance: Long = initialBalance.value

                    var t: FXTransactionDetail? = null

                    while (rs.next()) {

                        val transactionId = rs.getLong("TRANSACTION_ID")
                        val date = rs.getDate("TRANSACTION_DATE")
                        val payee: String? = rs.getString("PAYEE_NAME")
                        val transferAmount = rs.getLong("TRANSFER_AMOUNT")
                        val sourceAccountId = rs.getLong("SOURCE_ACCOUNT_ID")
                        val sourceAccountTypeCategory = rs.getString("SOURCE_ACCOUNT_TYPE_CATEGORY")
                        val sourceAccountName = rs.getString("SOURCE_ACCOUNT_NAME")
                        val targetAccountId = rs.getLongOrNull("TARGET_ACCOUNT_ID")
                        val targetAccountTypeCategory = rs.getString("TARGET_ACCOUNT_TYPE_CATEGORY")
                        val targetAccountName = rs.getString("TARGET_ACCOUNT_NAME")
                        val categoryName = rs.getString("CATEGORY_NAME")
                        val memo: String? = rs.getString("TRANSACTION_MEMO")

                        t?.run {
                            // start new transaction
                            if (identity != transactionId) {
                                balance += amount
                                addTo(list, category, amount, balance)
                                category = null
                                amount = 0
                                t = FXTransactionDetail(transactionId, date, payee, memo)
                            }
                        }

                        if (t == null) {
                            // first transaction
                            t = FXTransactionDetail(transactionId, date, payee, memo)
                        }

                        t?.run {

                            // calculate the total amount for this transaction
                            // this is not an else in case the source and target are the same

                            if (accountId == sourceAccountId) {
                                amount += transferAmount

                                category = when (category) {
                                    null -> categoryName(
                                            accountTypeCategory = AccountType.Category.valueOf(targetAccountTypeCategory),
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
                    }

                    t?.run {
                        // last transaction
                        balance += amount
                        addTo(list, category, amount, balance)
                    }
                }
            })

            return list
        }

        private fun FXTransactionDetail.addTo(list: MutableList<FXTransactionDetail>, category: String?, amount: Long, balance: Long) {
            this.category.value = category
            this.amount.value = Money.valueOf(amount)
            this.balance.value = Money.valueOf(balance)
            list.add(this)
        }
    }
}

fun Account.getTransactionDetails(database: MoneyDatabase): List<FXTransactionDetail> =
        FXTransactionDetail.getTransactionsForAccount(identity!!, initialBalance ?: Money.zero(), database)

package com.munzenberger.money.data.api.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.transaction.CategoryEntryId
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransferEntryId
import kotlinx.datetime.LocalDate

sealed class RegisterEntry {
    abstract val accountId: AccountId
    abstract val payeeId: PayeeId?
    abstract val payeeName: String?
    abstract val date: LocalDate
    abstract val amount: Money
    abstract val balance: Money
    abstract val number: String?
    abstract val memo: String?
    abstract val status: TransactionStatus

    data class Transaction(
        val transactionId: TransactionId,
        override val accountId: AccountId,
        override val payeeId: PayeeId?,
        override val payeeName: String?,
        override val date: LocalDate,
        override val amount: Money,
        override val balance: Money,
        override val number: String?,
        override val memo: String?,
        override val status: TransactionStatus,
        val entries: List<Entry>,
    ) : RegisterEntry() {

        sealed class Entry {
            abstract val amount: Money

            data class Category(
                val categoryEntryId: CategoryEntryId,
                val categoryId: CategoryId,
                val categoryName: String,
                val parentCategoryName: String?,
                override val amount: Money,
            ) : Entry()

            data class Transfer(
                val transferEntryId: TransferEntryId,
                val accountId: AccountId,
                val accountName: String,
                override val amount: Money,
            ) : Entry()
        }
    }

    data class Transfer(
        val transferEntryId: TransferEntryId,
        override val accountId: AccountId,
        override val payeeId: PayeeId?,
        override val payeeName: String?,
        override val date: LocalDate,
        override val amount: Money,
        override val balance: Money,
        override val number: String?,
        override val memo: String?,
        override val status: TransactionStatus
    ) : RegisterEntry()
}

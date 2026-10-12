package com.munzenberger.money.data.api.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransferEntryId
import kotlinx.datetime.LocalDate

sealed class RegisterEntry {
    abstract val accountId: AccountId
    abstract val payee: Payee?
    abstract val date: LocalDate
    abstract val amount: Money
    abstract val number: String?
    abstract val memo: String?
    abstract val status: TransactionStatus

    data class Transaction(
        val transactionId: TransactionId,
        override val accountId: AccountId,
        override val payee: Payee?,
        override val date: LocalDate,
        override val amount: Money,
        override val number: String?,
        override val memo: String?,
        override val status: TransactionStatus
    ) : RegisterEntry()

    data class Transfer(
        val transferEntryId: TransferEntryId,
        override val accountId: AccountId,
        override val payee: Payee?,
        override val date: LocalDate,
        override val amount: Money,
        override val number: String?,
        override val memo: String?,
        override val status: TransactionStatus
    ) : RegisterEntry()
}

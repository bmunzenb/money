package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.today
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@JvmInline
value class TransactionId(val value: Uuid = Uuid.random())

data class Transaction(
    val id: TransactionId = TransactionId(),
    val accountId: AccountId,
    val payeeId: PayeeId? = null,
    val date: LocalDate = LocalDate.today(),
    val number: String? = null,
    val memo: String? = null,
    val status: TransactionStatus,
)

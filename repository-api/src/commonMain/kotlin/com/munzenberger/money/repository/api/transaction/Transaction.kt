package com.munzenberger.money.repository.api.transaction

import com.munzenberger.money.repository.api.account.AccountId
import com.munzenberger.money.repository.api.payee.PayeeId
import com.munzenberger.money.repository.api.today
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class TransactionId(val value: Uuid = Uuid.random())

@OptIn(ExperimentalUuidApi::class)
data class Transaction(
    val id: TransactionId = TransactionId(),
    val accountId: AccountId,
    val payeeId: PayeeId? = null,
    val date: LocalDate = LocalDate.today(),
    val number: String? = null,
    val memo: String? = null,
    val status: TransactionStatus,
)

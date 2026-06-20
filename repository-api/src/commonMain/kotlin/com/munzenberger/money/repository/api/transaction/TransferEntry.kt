package com.munzenberger.money.repository.api.transaction

import com.munzenberger.money.repository.api.Money
import com.munzenberger.money.repository.api.account.AccountId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class TransferEntryId(val id: Uuid = Uuid.random())

@OptIn(ExperimentalUuidApi::class)
data class TransferEntry(
    val id: TransferEntryId = TransferEntryId(),
    val transactionId: TransactionId,
    val accountId: AccountId,
    val amount: Money,
    val number: String? = null,
    val memo: String? = null,
    val status: TransactionStatus,
    val orderInTransaction: Int,
)

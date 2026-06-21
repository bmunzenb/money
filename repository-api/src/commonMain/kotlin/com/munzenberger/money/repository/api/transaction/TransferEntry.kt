package com.munzenberger.money.repository.api.transaction

import com.munzenberger.money.repository.api.Money
import com.munzenberger.money.repository.api.account.AccountId
import kotlin.uuid.Uuid

@JvmInline
value class TransferEntryId(val id: Uuid = Uuid.random())

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

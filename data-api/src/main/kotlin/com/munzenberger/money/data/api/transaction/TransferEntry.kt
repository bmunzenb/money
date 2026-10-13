package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountId
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class TransferEntryId(val id: Uuid = Uuid.random())

data class TransferEntry(
    val id: TransferEntryId = TransferEntryId(),
    override val transactionId: TransactionId,
    val accountId: AccountId,
    override val amount: Money,
    val number: String? = null,
    override val memo: String? = null,
    val status: TransactionStatus,
    override val orderInTransaction: Int,
) : TransactionEntry

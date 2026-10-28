package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.today
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class TransactionId @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid = Uuid.generateV7())

data class Transaction(
    val id: TransactionId = TransactionId(),
    val accountId: AccountId,
    val payeeId: PayeeId? = null,
    val date: LocalDate = LocalDate.today(),
    val number: String? = null,
    val memo: String? = null,
    val status: TransactionStatus,
)

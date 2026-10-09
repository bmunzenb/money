package com.munzenberger.money.data.api.transaction

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class TransactionStatusId(val value: Long)

enum class TransactionStatusConstant {
    Unreconciled, Cleared, Reconciled
}

data class TransactionStatus(
    val id: TransactionStatusId,
    val value: TransactionStatusConstant
)

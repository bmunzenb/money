package com.munzenberger.money.data.api.payee

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class PayeeId(val value: Uuid = Uuid.random())

data class Payee(
    val id: PayeeId = PayeeId(),
    val name: String,
    val memo: String? = null,
)

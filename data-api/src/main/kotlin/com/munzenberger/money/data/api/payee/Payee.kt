package com.munzenberger.money.data.api.payee

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class PayeeId @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid = Uuid.generateV7())

data class Payee(
    val id: PayeeId = PayeeId(),
    val name: String,
    val memo: String? = null,
)

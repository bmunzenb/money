package com.munzenberger.money.repository.api.payee

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class PayeeId(val value: Uuid = Uuid.random())

@OptIn(ExperimentalUuidApi::class)
data class Payee(
    val id: PayeeId = PayeeId(),
    val name: String,
    val memo: String? = null,
)

package com.munzenberger.money.repository.api.payee

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class PayeeId(val value: Uuid = Uuid.random())

data class Payee(
    @OptIn(ExperimentalUuidApi::class)
    val id: PayeeId = PayeeId(),
    val name: String,
    val memo: String? = null,
)

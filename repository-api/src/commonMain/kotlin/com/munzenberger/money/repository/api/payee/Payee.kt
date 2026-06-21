package com.munzenberger.money.repository.api.payee

import kotlin.uuid.Uuid

@JvmInline
value class PayeeId(val value: Uuid = Uuid.random())

data class Payee(
    val id: PayeeId = PayeeId(),
    val name: String,
    val memo: String? = null,
)

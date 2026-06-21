package com.munzenberger.money.repository.api.bank

import kotlin.uuid.Uuid

@JvmInline
value class BankId(val value: Uuid = Uuid.random())

data class Bank(
    val id: BankId = BankId(),
    val name: String,
    val memo: String? = null,
)

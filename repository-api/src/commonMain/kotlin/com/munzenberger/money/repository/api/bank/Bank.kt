package com.munzenberger.money.repository.api.bank

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class BankId(val value: Uuid = Uuid.random())

@OptIn(ExperimentalUuidApi::class)
data class Bank(
    val id: BankId = BankId(),
    val name: String,
    val memo: String? = null,
)

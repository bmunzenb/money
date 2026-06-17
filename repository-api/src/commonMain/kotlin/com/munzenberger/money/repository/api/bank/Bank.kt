package com.munzenberger.money.repository.api.bank

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class BankId(val value: Uuid = Uuid.random())

data class Bank(
    @OptIn(ExperimentalUuidApi::class)
    val id: BankId = BankId(),
    val name: String,
    val memo: String?
)

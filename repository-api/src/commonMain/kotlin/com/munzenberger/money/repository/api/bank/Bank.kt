package com.munzenberger.money.repository.api.bank

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class BankId(val value: Uuid)

data class Bank(
    @OptIn(ExperimentalUuidApi::class)
    val id: BankId,
    val name: String,
    val memo: String?
)

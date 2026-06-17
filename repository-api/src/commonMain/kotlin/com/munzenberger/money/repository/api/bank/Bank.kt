package com.munzenberger.money.repository.api.bank

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class BankId(val value: Uuid)

@OptIn(ExperimentalUuidApi::class)
data class Bank(
    val id: BankId,
    val name: String,
    val memo: String?
)

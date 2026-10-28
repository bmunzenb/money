package com.munzenberger.money.data.api.bank

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class BankId @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid = Uuid.generateV7())

data class Bank(
    val id: BankId = BankId(),
    val name: String,
    val memo: String? = null,
)

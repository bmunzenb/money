package com.munzenberger.money.data.api.bank

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class BankId(val value: Uuid = Uuid.random())

data class Bank(
    val id: BankId = BankId(),
    val name: String,
    val memo: String? = null,
)

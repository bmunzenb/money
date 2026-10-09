package com.munzenberger.money.data.api.account

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.bank.BankId
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class AccountId(val value: Uuid = Uuid.random())

data class Account(
    val id: AccountId = AccountId(),
    val name: String,
    val number: String? = null,
    val accountType: AccountType,
    val bankId: BankId? = null,
    val initialBalance: Money = Money(0),
    val memo: String? = null,
)

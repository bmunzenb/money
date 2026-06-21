package com.munzenberger.money.repository.api.account

import com.munzenberger.money.repository.api.Money
import com.munzenberger.money.repository.api.bank.BankId
import kotlin.uuid.Uuid

@JvmInline
value class AccountId(val value: Uuid = Uuid.random())

data class Account(
    val id: AccountId = AccountId(),
    val name: String,
    val number: String? = null,
    val accountType: AccountType,
    val bankId: BankId? = null,
    val initialBalance: Money? = null,
)

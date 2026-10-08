package com.munzenberger.money.data.api.account

@JvmInline
value class AccountTypeId(val value: Long)

enum class AccountTypeConstant {
    Savings, Checking, Asset, Cash, Credit, Loan
}

data class AccountType(
    val id: AccountTypeId,
    val accountClass: AccountClass,
    val value: AccountTypeConstant
)

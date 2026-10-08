package com.munzenberger.money.data.api.account

@JvmInline
value class AccountClassId(val value: Long)

enum class AccountClassConstant {
    Assets, Liabilities
}

data class AccountClass(
    val id: AccountClassId,
    val value: AccountClassConstant
)

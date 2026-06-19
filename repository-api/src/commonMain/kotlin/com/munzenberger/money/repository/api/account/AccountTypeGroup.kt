package com.munzenberger.money.repository.api.account

@JvmInline
value class AccountTypeGroupId(val value: Long)

enum class AccountTypeGroupConstant {
    Assets, Liabilities
}

data class AccountTypeGroup(
    val id: AccountTypeGroupId,
    val value: AccountTypeGroupConstant
)

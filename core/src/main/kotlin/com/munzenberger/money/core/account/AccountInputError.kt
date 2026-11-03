package com.munzenberger.money.core.account

/** Why an [AccountInput] can't be saved. Each error is for a single field. */
enum class AccountInputError {
    BlankName,
    MissingAccountType,
    InvalidInitialBalance,
}

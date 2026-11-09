package com.munzenberger.money.core.register

/** Which statuses of register entries [GetAccountRegisterUseCase] returns. */
enum class RegisterStatusFilter {
    AllTransactions,

    /** Entries that aren't reconciled, whether or not they're cleared. */
    UnreconciledTransactions,
}

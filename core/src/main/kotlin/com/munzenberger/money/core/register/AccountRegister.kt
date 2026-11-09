package com.munzenberger.money.core.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.register.RegisterEntry

/**
 * An account's register, as [GetAccountRegisterUseCase] returns it. [entries] are the register entries that match the
 * filters, and [endingBalance] is the account's balance after all of its entries, whether or not they match, or null
 * if it has none.
 */
data class AccountRegister(
    val entries: List<RegisterEntry>,
    val endingBalance: Money?,
)

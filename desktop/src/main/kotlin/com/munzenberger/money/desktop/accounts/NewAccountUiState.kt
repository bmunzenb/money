package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank

data class NewAccountUiState(
    val name: String = "",
    val accountTypes: LoadState<List<AccountType>> = LoadState.Loading,
    val accountType: AccountType? = null,
    val banks: LoadState<List<Bank>> = LoadState.Loading,
    /** The text in the financial institution field: an existing bank's name, or a new one typed in. */
    val bankName: String = "",
    /** The existing bank matching [bankName], or null if the name is blank or for a new bank. */
    val bank: Bank? = null,
    val number: String = "",
    /** The text in the initial balance field. Blank means a zero balance. */
    val initialBalance: String = "",
    /** The symbol of the initial balance's currency, shown before the amount (e.g. "$"). */
    val currencySymbol: String,
    /** Whether [initialBalance] should be shown as invalid; only set once the user leaves the field. */
    val isInitialBalanceError: Boolean = false,
)

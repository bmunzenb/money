package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank

/** The account form, for adding a new account or editing an existing one. */
data class AccountFormUiState(
    /**
     * The account being edited, as it was when the form was filled in from it, or null when adding a new
     * account. The fields can't be shown until it's loaded.
     */
    val existingAccount: LoadState<Account>? = null,
    val name: String = "",
    /** Whether [name] should be shown as invalid; only set once the user tries to save. */
    val isNameError: Boolean = false,
    val accountTypes: LoadState<List<AccountType>> = LoadState.Loading,
    val accountType: AccountType? = null,
    /** Whether the account type should be shown as missing; only set once the user tries to save. */
    val isAccountTypeError: Boolean = false,
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
    val memo: String = "",
    /**
     * Whether [initialBalance] should be shown as invalid; only set once the user leaves the field or
     * tries to save.
     */
    val isInitialBalanceError: Boolean = false,
    val saveState: SaveState = SaveState.Idle,
) {
    val isEditing: Boolean
        get() = existingAccount != null

    /** Whether the fields can be shown: always for a new account, or once the account being edited is loaded. */
    val isFormReady: Boolean
        get() = existingAccount == null || existingAccount is LoadState.Loaded
}

enum class SaveState {
    Idle,

    /** The account is being written to the repository; the form can't be edited until it's done. */
    Saving,

    /** The account was valid, but couldn't be written. The form can be edited and saved again. */
    Failed,
}

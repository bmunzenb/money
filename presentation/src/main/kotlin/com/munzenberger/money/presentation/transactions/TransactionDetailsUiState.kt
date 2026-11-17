package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.core.transaction.TransactionWithEntries
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.presentation.state.LoadState
import kotlinx.datetime.LocalDate

/** The transaction form, for adding a new transaction or editing an existing one. */
data class TransactionDetailsUiState(
    /**
     * The transaction being edited, with its entries, as it was when the form was filled in from it, or null when
     * adding a new transaction. The fields can't be shown until it's loaded.
     */
    val existingTransaction: LoadState<TransactionWithEntries>? = null,
    /** The accounts the transaction can be in, sorted by name. */
    val accounts: LoadState<List<Account>> = LoadState.Loading,
    /** The account selected from [accounts], or null if none is selected yet. */
    val account: Account? = null,
    /** The payees the transaction can be paid to, sorted by name. */
    val payees: LoadState<List<Payee>> = LoadState.Loading,
    /**
     * The text in the payee field: an existing payee's name, or a new one typed in. Empty for a new transaction, or
     * the saved payee's name of the one being edited once it's loaded.
     */
    val payeeName: String = "",
    /** The existing payee matching [payeeName], or null if the name is blank or for a new payee. */
    val payee: Payee? = null,
    /** The transaction's date: today for a new transaction, or the saved date of the one being edited. */
    val date: LocalDate,
    /**
     * Whether the transaction raises or lowers the account's balance: a decrease for a new transaction, or the type of
     * the one being edited's total once it's loaded.
     */
    val type: TransactionType = TransactionType.Decrease,
    /**
     * The transaction's number, such as a check number: empty for a new transaction, or the saved number of the one
     * being edited once it's loaded.
     */
    val number: String = "",
    /**
     * The categories and transfer accounts the transaction's entries can be assigned to: the categories, each after
     * its parent, then the accounts, each sorted by name.
     */
    val categories: LoadState<List<TransactionCategory>> = LoadState.Loading,
    /**
     * The rows of the transaction's split, as the user has entered them, in order. Loading while they're being filled
     * in from the transaction being edited.
     */
    val entries: LoadState<List<TransactionEntryInput>> = LoadState.Loading,
) {
    val isEditing: Boolean
        get() = existingTransaction != null

    /**
     * Whether the fields can be shown: always for a new transaction, or once the transaction being edited is loaded.
     */
    val isFormReady: Boolean
        get() = existingTransaction == null || existingTransaction is LoadState.Loaded
}

package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.core.transaction.TransactionWithEntries
import com.munzenberger.money.presentation.state.LoadState

/** The transaction form, for adding a new transaction or editing an existing one. */
data class TransactionDetailsUiState(
    /**
     * The transaction being edited, with its entries, as it was when the form was filled in from it, or null when
     * adding a new transaction. The fields can't be shown until it's loaded.
     */
    val existingTransaction: LoadState<TransactionWithEntries>? = null,
) {
    val isEditing: Boolean
        get() = existingTransaction != null

    /**
     * Whether the fields can be shown: always for a new transaction, or once the transaction being edited is loaded.
     */
    val isFormReady: Boolean
        get() = existingTransaction == null || existingTransaction is LoadState.Loaded
}

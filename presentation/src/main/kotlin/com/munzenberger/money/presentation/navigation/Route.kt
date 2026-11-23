package com.munzenberger.money.presentation.navigation

import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransferEntryId
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object AccountList : Route

    /** A route to the account form: [NewAccount] adds an account, and [AccountSettings] edits one. */
    @Serializable
    sealed interface AccountFormRoute : Route

    @Serializable
    data object NewAccount : AccountFormRoute

    @Serializable
    data class AccountSettings(val accountId: AccountId) : AccountFormRoute

    @Serializable
    data object CategoryList : Route

    @Serializable
    data object PayeeList : Route

    @Serializable
    data class AccountRegister(val accountId: AccountId) : Route

    @Serializable
    sealed interface TransactionFormRoute : Route

    /** Adds a transaction to [accountId], or to an account the user chooses when it's null. */
    @Serializable
    data class NewTransaction(val accountId: AccountId? = null) : TransactionFormRoute

    @Serializable
    data class TransactionDetails(val transactionId: TransactionId) : TransactionFormRoute

    /**
     * Edits the split of the transaction in the form at [form]. It's shown over the form and edits the form's own
     * entries, so its screen shares the form's ViewModel rather than having its own.
     */
    @Serializable
    data class TransactionSplit(val form: TransactionFormRoute) : Route

    @Serializable
    data class TransferDetails(val transferEntryId: TransferEntryId) : Route

    @Serializable
    data object Portfolio : Route

    @Serializable
    data object Schedule : Route
}

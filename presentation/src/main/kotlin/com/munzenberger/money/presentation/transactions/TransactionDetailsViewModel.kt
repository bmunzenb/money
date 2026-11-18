package com.munzenberger.money.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.category.CategoryWithParent
import com.munzenberger.money.core.category.GetCategoriesUseCase
import com.munzenberger.money.core.transaction.GetTransactionUseCase
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.data.api.today
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.TransactionEntry
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransferEntry
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.database.resultFlow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrEmpty
import com.munzenberger.money.presentation.state.loadedOrNull
import com.munzenberger.money.presentation.state.toLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

/**
 * The transaction form, shown at [route]. For [Route.TransactionDetails], it edits that transaction, starting from its
 * saved values; for [Route.NewTransaction], it adds a new transaction.
 */
// One handler per field, so the function count grows with the form.
@Suppress("TooManyFunctions")
class TransactionDetailsViewModel(
    // This screen's route, so going back only pops this screen, even if a save finishes after the user has left it.
    private val route: Route.TransactionFormRoute,
    private val repositoryController: MoneyRepositoryController,
    private val navigator: Navigator,
    private val getTransaction: GetTransactionUseCase,
    private val getCategories: GetCategoriesUseCase,
    // Formats each category as the UI shows it, to find the one the user picked from its text.
    private val categoryFormatter: TransactionCategoryFormatter,
    // The date a new transaction starts on.
    today: () -> LocalDate = { LocalDate.today() },
) : ViewModel() {

    private val transactionId: TransactionId? = (route as? Route.TransactionDetails)?.transactionId

    // The account a new transaction starts in, if the route has one.
    private val newTransactionAccountId: AccountId? = (route as? Route.NewTransaction)?.accountId

    private val _state = MutableStateFlow(
        TransactionDetailsUiState(
            existingTransaction = transactionId?.let { LoadState.Loading },
            // A new transaction starts with one empty row to fill in.
            entries = transactionId?.let { LoadState.Loading } ?: LoadState.Loaded(listOf(TransactionEntryInput())),
            date = today(),
        )
    )
    val state: StateFlow<TransactionDetailsUiState> = _state.asStateFlow()

    // Read once and shared, since they're the account options, the transfer categories, and the existing transaction's
    // transfer accounts. The latest is replayed so each collector gets it however they're scheduled.
    private val accounts = repositoryController.resultFlow { it.accounts }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    // Read once and shared like the accounts, since they're both the category options and the existing transaction's
    // categories.
    private val categories = repositoryController.flow { getCategories(it) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    init {
        viewModelScope.launch {
            accounts.collect { result ->
                val accounts = result.map { accounts -> accounts.sortedBy { it.name.lowercase() } }.toLoadState()
                _state.update { it.copy(accounts = accounts).withInitialAccount() }
            }
        }
        viewModelScope.launch {
            repositoryController.resultFlow { it.payees }.collect { result ->
                val payees = result.map { payees -> payees.sortedBy { it.name.lowercase() } }.toLoadState()
                _state.update { it.copy(payees = payees, payee = payees.loadedOrEmpty.matching(it.payeeName)) }
            }
        }
        viewModelScope.launch {
            combine(
                categories,
                accounts,
                ::transactionCategories,
            ).collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
        if (transactionId != null) {
            viewModelScope.launch { loadExistingTransaction(transactionId) }
        }
    }

    /**
     * Fills in the form from the saved transaction. It's read only once, so a later change to the transaction
     * doesn't overwrite what the user has typed. The payees are needed too, to show the transaction's payee by
     * name; without them, saving would drop the payee, so the transaction counts as not loaded. The same goes for the
     * categories and accounts, to fill in each entry's row.
     */
    private suspend fun loadExistingTransaction(transactionId: TransactionId) {
        val transaction = repositoryController.flow { getTransaction(it, transactionId) }.first().getOrNull()
        val payees = repositoryController.resultFlow { it.payees }.first().getOrNull()
        if (transaction == null || payees == null) return onExistingTransactionLoadFailed()

        val categories = categories.first().getOrNull()
        val accounts = accounts.first().getOrNull()
        if (categories == null || accounts == null) return onExistingTransactionLoadFailed()

        val payee = payees.firstOrNull { it.id == transaction.transaction.payeeId }
        val type = TransactionType.of(transaction.amount)
        val entries = transaction.entries.map { it.toInput(type, categories, accounts, categoryFormatter) }
        _state.update { state ->
            state.copy(
                existingTransaction = LoadState.Loaded(transaction),
                payeeName = payee?.name.orEmpty(),
                payee = payee,
                date = transaction.transaction.date,
                type = type,
                number = transaction.transaction.number.orEmpty(),
                entries = LoadState.Loaded(entries),
            ).withInitialAccount()
        }
    }

    private fun onExistingTransactionLoadFailed() {
        _state.update { it.copy(existingTransaction = LoadState.Error, entries = LoadState.Error) }
    }

    /**
     * Selects the account the form starts in, once both it and the accounts are loaded: the existing transaction's
     * account, or the new transaction route's. Only while none is selected, so a reload keeps the user's choice.
     */
    private fun TransactionDetailsUiState.withInitialAccount(): TransactionDetailsUiState {
        if (account != null) return this
        val initialAccountId = existingTransaction?.loadedOrNull?.transaction?.accountId ?: newTransactionAccountId
        return copy(account = accounts.loadedOrEmpty.find { it.id == initialAccountId })
    }

    /**
     * The categories, each after its parent, followed by the accounts to transfer to or from, each sorted by name. If
     * either couldn't be read, none are shown.
     */
    private fun transactionCategories(
        categories: Result<List<CategoryWithParent>>,
        accounts: Result<List<Account>>,
    ): LoadState<List<TransactionCategory>> {
        val categories = categories.getOrNull()
        val accounts = accounts.getOrNull()
        if (categories == null || accounts == null) return LoadState.Error

        val sortedCategories = categories.sortedWith(
            compareBy(
                { (it.parent ?: it.category).name.lowercase() },
                { it.parent != null },
                { it.category.name.lowercase() },
            )
        )
        return LoadState.Loaded(
            sortedCategories.map { TransactionCategory.Category(it) } +
                accounts.sortedBy { it.name.lowercase() }.map { TransactionCategory.Transfer(it) }
        )
    }

    fun onAccountChange(account: Account) {
        _state.update { it.copy(account = account) }
    }

    fun onPayeeNameChange(payeeName: String) {
        _state.update { it.copy(payeeName = payeeName, payee = it.payees.loadedOrEmpty.matching(payeeName)) }
    }

    fun onPayeeChange(payee: Payee) {
        _state.update { it.copy(payeeName = payee.name, payee = payee) }
    }

    fun onDateChange(date: LocalDate) {
        _state.update { it.copy(date = date) }
    }

    fun onTypeChange(type: TransactionType) {
        _state.update { it.copy(type = type) }
    }

    fun onNumberChange(number: String) {
        _state.update { it.copy(number = number) }
    }

    /**
     * Sets the entry with [key]'s category text, and selects the category or transfer account shown as [categoryText],
     * ignoring case and surrounding whitespace. Text that doesn't match one of the categories is kept, as a new
     * category's name, and leaves the entry without a category.
     */
    fun onEntryCategoryChange(key: Uuid, categoryText: String) {
        _state.update { state ->
            val category = state.categories.loadedOrEmpty.matching(categoryText)
            state.withEntry(key) { it.copy(categoryText = categoryText, category = category) }
        }
    }

    fun onEntryAmountChange(key: Uuid, amountText: String) {
        _state.update { state -> state.withEntry(key) { it.copy(amountText = amountText) } }
    }

    fun onEntryMemoChange(key: Uuid, memo: String) {
        _state.update { state -> state.withEntry(key) { it.copy(memo = memo) } }
    }

    /** Adds an empty row after the last one. */
    fun onAddEntryClick() {
        _state.update { state -> state.withEntries { it + TransactionEntryInput() } }
    }

    /**
     * Removes the row with [key]. Removing the only row leaves an empty one in its place, so there's always a row to
     * fill in.
     */
    fun onRemoveEntryClick(key: Uuid) {
        _state.update { state ->
            state.withEntries { entries ->
                entries.filterNot { it.key == key }.ifEmpty { listOf(TransactionEntryInput()) }
            }
        }
    }

    /** The category shown as [categoryText], or null if there isn't one. */
    private fun List<TransactionCategory>.matching(categoryText: String): TransactionCategory? {
        val text = categoryText.trim()
        if (text.isEmpty()) return null
        return firstOrNull { categoryFormatter.format(it).equals(text, ignoreCase = true) }
    }

    fun onBackClick() {
        navigator.back(from = route)
    }
}

/**
 * This state with the entry with [key] changed by [transform]. Unchanged if the entries aren't loaded or none has [key],
 * such as a row the user removed.
 */
private fun TransactionDetailsUiState.withEntry(
    key: Uuid,
    transform: (TransactionEntryInput) -> TransactionEntryInput,
): TransactionDetailsUiState = withEntries { entries -> entries.map { if (it.key == key) transform(it) else it } }

/** This state with its rows changed by [transform]. Unchanged if the entries aren't loaded. */
private fun TransactionDetailsUiState.withEntries(
    transform: (List<TransactionEntryInput>) -> List<TransactionEntryInput>,
): TransactionDetailsUiState {
    val entries = entries.loadedOrNull ?: return this
    return copy(entries = LoadState.Loaded(transform(entries)))
}

/**
 * Typing an existing payee's name selects that payee rather than starting a new one, so the user doesn't
 * end up with a duplicate just because they typed instead of picking from the list.
 */
private fun List<Payee>.matching(payeeName: String): Payee? {
    val name = payeeName.trim()
    return if (name.isEmpty()) null else firstOrNull { it.name.equals(name, ignoreCase = true) }
}

/**
 * The row for this saved entry, with its amount as entered for a transaction of [type], so it saves back unchanged. An
 * entry whose category or account isn't among [categories] or [accounts] has no category, so the user picks one. Its
 * category text is the category formatted by [categoryFormatter], or empty if it has none.
 */
private fun TransactionEntry.toInput(
    type: TransactionType,
    categories: List<CategoryWithParent>,
    accounts: List<Account>,
    categoryFormatter: TransactionCategoryFormatter,
): TransactionEntryInput {
    val amountText = (if (type == TransactionType.Decrease) -amount else amount).toString(asCurrency = false)
    val input = when (this) {
        is CategoryEntry -> TransactionEntryInput(
            category = categories.find { it.category.id == categoryId }?.let { TransactionCategory.Category(it) },
            amountText = amountText,
            memo = memo.orEmpty(),
        )
        is TransferEntry -> TransactionEntryInput(
            category = accounts.find { it.id == accountId }?.let { TransactionCategory.Transfer(it) },
            amountText = amountText,
            memo = memo.orEmpty(),
            transferNumber = number.orEmpty(),
            transferStatus = status,
        )
    }
    return input.copy(categoryText = input.category?.let(categoryFormatter::format).orEmpty())
}

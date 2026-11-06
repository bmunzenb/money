package com.munzenberger.money.data.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountRepository
import com.munzenberger.money.data.api.account.AccountWriter
import com.munzenberger.money.data.api.bank.BankId
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.requireOneRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

internal class SqlAccountRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : AccountRepository, AccountWriter {

    override val accounts: Flow<List<Account>> = database.accountQueries
        .selectAll(::account)
        .asFlow()
        .mapToList(context)

    override fun accountById(accountId: AccountId): Flow<Account?> = database.accountQueries
        .selectById(accountId.value, ::account)
        .asFlow()
        .mapToOneOrNull(context)

    /**
     * Emits the balance of each account, keyed by its id: its initial balance plus the entries of its own
     * transactions, minus the transfer entries of other accounts' transactions that move money into or out of it.
     */
    override val accountBalances: Flow<Map<AccountId, Money>> = database.accountQueries
        .selectBalances { id, balance -> AccountId(id) to Money(balance) }
        .asFlow()
        .mapToList(context)
        .map { it.toMap() }

    /** Emits the balance of the account with [accountId], calculated as in [accountBalances], or null if it's gone. */
    override fun accountBalanceById(accountId: AccountId): Flow<Money?> = database.accountQueries
        .selectBalanceById(accountId.value)
        .asFlow()
        .mapToOneOrNull(context)
        .map { balance -> balance?.let(::Money) }

    override fun add(account: Account) {
        database.accountQueries.insert(
            id = account.id.value,
            name = account.name,
            number = account.number,
            account_type_id = account.accountType.id.value,
            bank_id = account.bankId?.value,
            initial_balance = account.initialBalance.value,
            memo = account.memo,
            is_closed = account.isClosed,
        )
    }

    override fun update(account: Account) {
        database.accountQueries.update(
            name = account.name,
            number = account.number,
            account_type_id = account.accountType.id.value,
            bank_id = account.bankId?.value,
            initial_balance = account.initialBalance.value,
            memo = account.memo,
            is_closed = account.isClosed,
            id = account.id.value,
        ).requireOneRow { "No account with id ${account.id.value}" }
    }

    override fun removeById(accountId: AccountId) {
        database.accountQueries.deleteById(accountId.value)
    }
}

/** Maps a row of the `AccountWithType` view, which `selectAll` and `selectById` select from, to an [Account]. */
@Suppress("LongParameterList")
private fun account(
    id: Uuid,
    name: String,
    number: String?,
    bankId: Uuid?,
    initialBalance: Long,
    memo: String?,
    isClosed: Boolean,
    typeId: Long,
    typeValue: String,
    typeClassId: Long,
    typeClassValue: String,
) = Account(
    id = AccountId(id),
    name = name,
    number = number,
    accountType = accountType(typeId, typeValue, typeClassId, typeClassValue),
    bankId = bankId?.let { BankId(it) },
    initialBalance = Money(initialBalance),
    memo = memo,
    isClosed = isClosed,
)

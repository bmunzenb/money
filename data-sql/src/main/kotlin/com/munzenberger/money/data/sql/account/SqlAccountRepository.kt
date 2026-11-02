package com.munzenberger.money.data.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneNotNull
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountRepository
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.account.AccountWriter
import com.munzenberger.money.data.api.bank.BankId
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.requireOneRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class SqlAccountRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : AccountRepository, AccountWriter {

    override val accounts: Flow<List<Account>> = database.accountQueries
        .selectAll {
                id, name, number, bankId, initialBalance, memo, isClosed,
                typeId, typeValue, typeClassId, typeClassValue,
            ->
            Account(
                id = AccountId(id),
                name = name,
                number = number,
                accountType = AccountType(
                    id = AccountTypeId(typeId),
                    accountClass = AccountClass(
                        id = AccountClassId(typeClassId),
                        value = AccountClassConstant.valueOf(typeClassValue),
                    ),
                    value = AccountTypeConstant.valueOf(typeValue),
                ),
                bankId = bankId?.let { BankId(it) },
                initialBalance = Money(initialBalance),
                memo = memo,
                isClosed = isClosed,
            )
        }
        .asFlow()
        .mapToList(context)

    /**
     * Emits the balance of [accountId]: its initial balance plus the entries of its own transactions, minus
     * the transfer entries of other accounts' transactions that move money into or out of it. Emits nothing
     * while the account doesn't exist.
     */
    override fun accountBalance(accountId: AccountId): Flow<Money> = database.accountQueries
        .selectBalance(accountId.value)
        .asFlow()
        .mapToOneNotNull(context)
        .map { Money(it) }

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

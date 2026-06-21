package com.munzenberger.money.repository.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.repository.api.Money
import com.munzenberger.money.repository.api.account.Account
import com.munzenberger.money.repository.api.account.AccountId
import com.munzenberger.money.repository.api.account.AccountRepository
import com.munzenberger.money.repository.api.account.AccountType
import com.munzenberger.money.repository.api.account.AccountTypeConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroup
import com.munzenberger.money.repository.api.account.AccountTypeGroupConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroupId
import com.munzenberger.money.repository.api.account.AccountTypeId
import com.munzenberger.money.repository.api.bank.BankId
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlAccountRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : AccountRepository {

    override val accounts: Flow<List<Account>> = database.accountQueries
        .selectAll { id, name, number, bankId, initialBalance, typeId, typeValue, typeGroupId, typeGroupValue ->
            Account(
                id = AccountId(Uuid.parse(id)),
                name = name,
                number = number,
                accountType = AccountType(
                    id = AccountTypeId(typeId),
                    group = AccountTypeGroup(
                        id = AccountTypeGroupId(typeGroupId),
                        value = AccountTypeGroupConstant.valueOf(typeGroupValue),
                    ),
                    value = AccountTypeConstant.valueOf(typeValue),
                ),
                bankId = bankId?.let { BankId(Uuid.parse(it)) },
                initialBalance = initialBalance?.let { Money(it) },
            )
        }
        .asFlow()
        .mapToList(context)

    override suspend fun add(account: Account) {
        withContext(context) {
            database.accountQueries.insert(
                id = account.id.value.toString(),
                name = account.name,
                number = account.number,
                account_type_id = account.accountType.id.value,
                bank_id = account.bankId?.value?.toString(),
                initial_balance = account.initialBalance?.value,
            )
        }
    }

    override suspend fun update(account: Account) {
        withContext(context) {
            database.accountQueries.update(
                name = account.name,
                number = account.number,
                account_type_id = account.accountType.id.value,
                bank_id = account.bankId?.value?.toString(),
                initial_balance = account.initialBalance?.value,
                id = account.id.value.toString(),
            )
        }
    }

    override suspend fun removeById(accountId: AccountId) {
        withContext(context) {
            database.accountQueries.deleteById(accountId.value.toString())
        }
    }
}

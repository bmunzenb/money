package com.munzenberger.money.data.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlAccountRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : AccountRepository, AccountWriter {

    override val accounts: Flow<List<Account>> = database.accountQueries
        .selectAll { id, name, number, bankId, initialBalance, memo, typeId, typeValue, typeClassId, typeClassValue ->
            Account(
                id = AccountId(Uuid.parse(id)),
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
                bankId = bankId?.let { BankId(Uuid.parse(it)) },
                initialBalance = Money(initialBalance),
                memo = memo,
            )
        }
        .asFlow()
        .mapToList(context)

    override fun add(account: Account) {
        database.accountQueries.insert(
            id = account.id.value.toString(),
            name = account.name,
            number = account.number,
            account_type_id = account.accountType.id.value,
            bank_id = account.bankId?.value?.toString(),
            initial_balance = account.initialBalance.value,
            memo = account.memo,
        )
    }

    override fun update(account: Account) {
        database.accountQueries.update(
            name = account.name,
            number = account.number,
            account_type_id = account.accountType.id.value,
            bank_id = account.bankId?.value?.toString(),
            initial_balance = account.initialBalance.value,
            memo = account.memo,
            id = account.id.value.toString(),
        )
    }

    override fun removeById(accountId: AccountId) {
        database.accountQueries.deleteById(accountId.value.toString())
    }
}

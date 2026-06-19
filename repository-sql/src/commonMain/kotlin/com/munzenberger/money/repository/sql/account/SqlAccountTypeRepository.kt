package com.munzenberger.money.repository.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.repository.api.account.AccountType
import com.munzenberger.money.repository.api.account.AccountTypeConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroup
import com.munzenberger.money.repository.api.account.AccountTypeGroupConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroupId
import com.munzenberger.money.repository.api.account.AccountTypeId
import com.munzenberger.money.repository.api.account.AccountTypeRepository
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

class SqlAccountTypeRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : AccountTypeRepository {

    override val accountTypes: Flow<List<AccountType>> = database.accountTypeQueries
        .selectAll { id, value, groupId, groupValue ->
            AccountType(
                id = AccountTypeId(id),
                group = AccountTypeGroup(
                    id = AccountTypeGroupId(groupId),
                    value = AccountTypeGroupConstant.valueOf(groupValue),
                ),
                value = AccountTypeConstant.valueOf(value),
            )
        }
        .asFlow()
        .mapToList(context)
}

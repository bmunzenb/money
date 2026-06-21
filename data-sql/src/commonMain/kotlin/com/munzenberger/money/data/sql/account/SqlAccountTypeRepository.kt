package com.munzenberger.money.data.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.account.AccountTypeRepository
import com.munzenberger.money.data.sql.MoneyDatabase
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

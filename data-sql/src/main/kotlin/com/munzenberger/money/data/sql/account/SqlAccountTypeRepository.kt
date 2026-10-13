package com.munzenberger.money.data.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
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
        .selectAll { id, value, classId, classValue ->
            AccountType(
                id = AccountTypeId(id),
                accountClass = AccountClass(
                    id = AccountClassId(classId),
                    value = AccountClassConstant.valueOf(classValue),
                ),
                value = AccountTypeConstant.valueOf(value),
            )
        }
        .asFlow()
        .mapToList(context)
}

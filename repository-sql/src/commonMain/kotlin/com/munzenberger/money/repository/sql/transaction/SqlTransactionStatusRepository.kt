package com.munzenberger.money.repository.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.repository.api.transaction.TransactionStatus
import com.munzenberger.money.repository.api.transaction.TransactionStatusConstant
import com.munzenberger.money.repository.api.transaction.TransactionStatusId
import com.munzenberger.money.repository.api.transaction.TransactionStatusRepository
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

class SqlTransactionStatusRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : TransactionStatusRepository {

    override val transactionStatuses: Flow<List<TransactionStatus>> = database.transactionStatusQueries
        .selectAll { id, value ->
            TransactionStatus(
                id = TransactionStatusId(id),
                value = TransactionStatusConstant.valueOf(value),
            )
        }
        .asFlow()
        .mapToList(context)
}

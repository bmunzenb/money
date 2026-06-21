package com.munzenberger.money.data.sql.payee

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.payee.PayeeRepository
import com.munzenberger.money.data.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlPayeeRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : PayeeRepository {

    override val payees: Flow<List<Payee>> = database.payeeQueries
        .selectAll { id, name, memo ->
            Payee(
                id = PayeeId(Uuid.parse(id)),
                name = name,
                memo = memo,
            )
        }
        .asFlow()
        .mapToList(context)

    override suspend fun add(payee: Payee) {
        withContext(context) {
            database.payeeQueries.insert(
                id = payee.id.value.toString(),
                name = payee.name,
                memo = payee.memo,
            )
        }
    }

    override suspend fun update(payee: Payee) {
        withContext(context) {
            database.payeeQueries.update(
                name = payee.name,
                memo = payee.memo,
                id = payee.id.value.toString(),
            )
        }
    }

    override suspend fun removeById(payeeId: PayeeId) {
        withContext(context) {
            database.payeeQueries.deleteById(payeeId.value.toString())
        }
    }
}

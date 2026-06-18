package com.munzenberger.money.repository.sqlite

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.munzenberger.money.repository.api.ModelState
import com.munzenberger.money.repository.api.bank.Bank
import com.munzenberger.money.repository.api.bank.BankId
import com.munzenberger.money.repository.api.bank.BanksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SqliteBanksRepository(
    driver: SqlDriver,
    private val context: CoroutineContext = Dispatchers.IO,
) : BanksRepository {

    private val database = MoneyDatabase(driver)

    override val banks: Flow<ModelState<List<Bank>>> = database.bankQueries
        .selectAll { id, name, memo ->
            Bank(
                id = BankId(Uuid.parse(id)),
                name = name,
                memo = memo,
            )
        }
        .asFlow()
        .mapToList(context)
        .map<List<Bank>, ModelState<List<Bank>>> { ModelState.Success(it) }
        .onStart { emit(ModelState.Loading()) }
        .catch { emit(ModelState.Error(it)) }

    override suspend fun create(bank: Bank) {
        withContext(context) {
            database.bankQueries.insert(
                id = bank.id.value.toString(),
                name = bank.name,
                memo = bank.memo,
            )
        }
    }

    override suspend fun update(bank: Bank) {
        withContext(context) {
            database.bankQueries.update(
                name = bank.name,
                memo = bank.memo,
                id = bank.id.value.toString(),
            )
        }
    }

    override suspend fun delete(id: BankId) {
        withContext(context) {
            database.bankQueries.deleteById(id.value.toString())
        }
    }
}

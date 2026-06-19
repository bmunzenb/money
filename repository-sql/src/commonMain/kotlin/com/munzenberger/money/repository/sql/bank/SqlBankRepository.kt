package com.munzenberger.money.repository.sql.bank

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.munzenberger.money.repository.api.bank.Bank
import com.munzenberger.money.repository.api.bank.BankId
import com.munzenberger.money.repository.api.bank.BankRepository
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SqlBankRepository(
    driver: SqlDriver,
    private val context: CoroutineContext = Dispatchers.IO,
) : BankRepository {

    private val database = MoneyDatabase(driver)

    override val banks: Flow<List<Bank>> = database.bankQueries
        .selectAll { id, name, memo ->
            Bank(
                id = BankId(Uuid.parse(id)),
                name = name,
                memo = memo,
            )
        }
        .asFlow()
        .mapToList(context)

    override suspend fun add(bank: Bank) {
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

    override suspend fun removeById(bankId: BankId) {
        withContext(context) {
            database.bankQueries.deleteById(bankId.value.toString())
        }
    }
}

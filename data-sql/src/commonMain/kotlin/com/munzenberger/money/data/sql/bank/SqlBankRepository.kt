package com.munzenberger.money.data.sql.bank

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.bank.BankId
import com.munzenberger.money.data.api.bank.BankRepository
import com.munzenberger.money.data.api.bank.BankWriter
import com.munzenberger.money.data.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlBankRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : BankRepository, BankWriter {

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

    override fun add(bank: Bank) {
        database.bankQueries.insert(
            id = bank.id.value.toString(),
            name = bank.name,
            memo = bank.memo,
        )
    }

    override fun update(bank: Bank) {
        database.bankQueries.update(
            name = bank.name,
            memo = bank.memo,
            id = bank.id.value.toString(),
        )
    }

    override fun removeById(bankId: BankId) {
        database.bankQueries.deleteById(bankId.value.toString())
    }
}

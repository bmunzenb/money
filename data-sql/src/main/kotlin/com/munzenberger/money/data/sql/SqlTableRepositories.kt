package com.munzenberger.money.data.sql

import com.munzenberger.money.data.sql.account.SqlAccountRepository
import com.munzenberger.money.data.sql.account.SqlStatementRepository
import com.munzenberger.money.data.sql.bank.SqlBankRepository
import com.munzenberger.money.data.sql.category.SqlCategoryRepository
import com.munzenberger.money.data.sql.payee.SqlPayeeRepository
import com.munzenberger.money.data.sql.transaction.SqlCategoryEntryRepository
import com.munzenberger.money.data.sql.transaction.SqlTransactionRepository
import com.munzenberger.money.data.sql.transaction.SqlTransferEntryRepository
import kotlin.coroutines.CoroutineContext

/** The repositories that both read and write, shared by [SqlMoneyRepository]'s reads and its writer. */
internal class SqlTableRepositories(database: MoneyDatabase, context: CoroutineContext) {
    val accounts = SqlAccountRepository(database, context)
    val statements = SqlStatementRepository(database, context)
    val banks = SqlBankRepository(database, context)
    val categories = SqlCategoryRepository(database, context)
    val payees = SqlPayeeRepository(database, context)
    val categoryEntries = SqlCategoryEntryRepository(database, context)
    val transactions = SqlTransactionRepository(database, context)
    val transferEntries = SqlTransferEntryRepository(database, context)
}

package com.munzenberger.money.data.sql

import app.cash.sqldelight.db.SqlDriver
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.AccountRepository
import com.munzenberger.money.data.api.account.AccountTypeRepository
import com.munzenberger.money.data.api.account.AccountWriter
import com.munzenberger.money.data.api.account.StatementRepository
import com.munzenberger.money.data.api.account.StatementWriter
import com.munzenberger.money.data.api.bank.BankRepository
import com.munzenberger.money.data.api.bank.BankWriter
import com.munzenberger.money.data.api.category.CategoryRepository
import com.munzenberger.money.data.api.category.CategoryTypeRepository
import com.munzenberger.money.data.api.category.CategoryWriter
import com.munzenberger.money.data.api.payee.PayeeRepository
import com.munzenberger.money.data.api.payee.PayeeWriter
import com.munzenberger.money.data.api.transaction.CategoryEntryRepository
import com.munzenberger.money.data.api.transaction.CategoryEntryWriter
import com.munzenberger.money.data.api.transaction.TransactionRepository
import com.munzenberger.money.data.api.transaction.TransactionStatusRepository
import com.munzenberger.money.data.api.transaction.TransactionWriter
import com.munzenberger.money.data.api.transaction.TransferEntryRepository
import com.munzenberger.money.data.api.transaction.TransferEntryWriter
import com.munzenberger.money.data.sql.account.SqlAccountRepository
import com.munzenberger.money.data.sql.account.SqlAccountTypeRepository
import com.munzenberger.money.data.sql.account.SqlStatementRepository
import com.munzenberger.money.data.sql.bank.SqlBankRepository
import com.munzenberger.money.data.sql.category.SqlCategoryRepository
import com.munzenberger.money.data.sql.category.SqlCategoryTypeRepository
import com.munzenberger.money.data.sql.payee.SqlPayeeRepository
import com.munzenberger.money.data.sql.transaction.SqlCategoryEntryRepository
import com.munzenberger.money.data.sql.transaction.SqlTransactionRepository
import com.munzenberger.money.data.sql.transaction.SqlTransactionStatusRepository
import com.munzenberger.money.data.sql.transaction.SqlTransferEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class SqlMoneyRepository private constructor(
    private val name: String,
    private val driver: SqlDriver,
    private val database: MoneyDatabase,
    private val context: CoroutineContext,
    tables: SqlTableRepositories,
) : MoneyRepository,
    AccountRepository by tables.accounts,
    AccountTypeRepository by SqlAccountTypeRepository(database, context),
    StatementRepository by tables.statements,
    BankRepository by tables.banks,
    CategoryRepository by tables.categories,
    CategoryTypeRepository by SqlCategoryTypeRepository(database, context),
    PayeeRepository by tables.payees,
    CategoryEntryRepository by tables.categoryEntries,
    TransactionRepository by tables.transactions,
    TransactionStatusRepository by SqlTransactionStatusRepository(database, context),
    TransferEntryRepository by tables.transferEntries
{
    constructor(
        name: String,
        driver: SqlDriver,
        database: MoneyDatabase = MoneyDatabase(driver),
        context: CoroutineContext = Dispatchers.IO,
    ) : this(name, driver, database, context, SqlTableRepositories(database, context))

    private val writer: MoneyWriter = object : MoneyWriter,
        AccountWriter by tables.accounts,
        StatementWriter by tables.statements,
        BankWriter by tables.banks,
        CategoryWriter by tables.categories,
        PayeeWriter by tables.payees,
        CategoryEntryWriter by tables.categoryEntries,
        TransactionWriter by tables.transactions,
        TransferEntryWriter by tables.transferEntries {}

    override suspend fun <R> transaction(block: MoneyWriter.() -> R): R =
        withContext(context) {
            database.transactionWithResult { writer.block() }
        }

    override fun close() {
        logger.info("Closing database: $name")
        driver.close()
    }
}

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

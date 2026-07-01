package com.munzenberger.money.data.sql

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountRepository
import com.munzenberger.money.data.api.account.AccountTypeRepository
import com.munzenberger.money.data.api.account.StatementRepository
import com.munzenberger.money.data.api.bank.BankRepository
import com.munzenberger.money.data.api.category.CategoryRepository
import com.munzenberger.money.data.api.category.CategoryTypeRepository
import com.munzenberger.money.data.api.payee.PayeeRepository
import com.munzenberger.money.data.api.transaction.CategoryEntryRepository
import com.munzenberger.money.data.api.transaction.TransactionRepository
import com.munzenberger.money.data.api.transaction.TransactionStatusRepository
import com.munzenberger.money.data.api.transaction.TransferEntryRepository
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
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.Properties
import kotlin.coroutines.CoroutineContext

class SqlMoneyRepository(
    database: MoneyDatabase,
    context: CoroutineContext = Dispatchers.IO,
) : MoneyRepository,
    AccountRepository by SqlAccountRepository(database, context),
    AccountTypeRepository by SqlAccountTypeRepository(database, context),
    StatementRepository by SqlStatementRepository(database, context),
    BankRepository by SqlBankRepository(database, context),
    CategoryRepository by SqlCategoryRepository(database, context),
    CategoryTypeRepository by SqlCategoryTypeRepository(database, context),
    PayeeRepository by SqlPayeeRepository(database, context),
    CategoryEntryRepository by SqlCategoryEntryRepository(database, context),
    TransactionRepository by SqlTransactionRepository(database, context),
    TransactionStatusRepository by SqlTransactionStatusRepository(database, context),
    TransferEntryRepository by SqlTransferEntryRepository(database, context) {

    companion object {
        fun open(file: File): SqlMoneyRepository {
            val isNew = !file.exists()
            val driver = JdbcSqliteDriver(
                url = "jdbc:sqlite:${file.absolutePath}",
                properties = Properties().apply { put("foreign_keys", "true") }
            )

            if (isNew) {
                MoneyDatabase.Schema.create(driver)
            }

            return SqlMoneyRepository(MoneyDatabase(driver))
        }
    }
}

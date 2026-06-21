package com.munzenberger.money.data.sql.account

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.Statement
import com.munzenberger.money.data.api.account.StatementId
import com.munzenberger.money.data.api.account.StatementRepository
import com.munzenberger.money.data.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlStatementRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : StatementRepository {

    override suspend fun statementsByAccountId(accountId: AccountId): Flow<List<Statement>> =
        database.statementQueries
            .selectByAccountId(accountId.value.toString()) { id, accountIdValue, closingDate, startingBalance, endingBalance, isReconciled ->
                Statement(
                    id = StatementId(Uuid.parse(id)),
                    accountId = AccountId(Uuid.parse(accountIdValue)),
                    closingDate = LocalDate.fromEpochDays(closingDate),
                    startingBalance = Money(startingBalance),
                    endingBalance = Money(endingBalance),
                    isReconciled = isReconciled,
                )
            }
            .asFlow()
            .mapToList(context)

    override suspend fun add(statement: Statement) {
        withContext(context) {
            database.statementQueries.insert(
                id = statement.id.id.toString(),
                account_id = statement.accountId.value.toString(),
                closing_date = statement.closingDate.toEpochDays(),
                starting_balance = statement.startingBalance.value,
                ending_balance = statement.endingBalance.value,
                is_reconciled = statement.isReconciled,
            )
        }
    }

    override suspend fun update(statement: Statement) {
        withContext(context) {
            database.statementQueries.update(
                account_id = statement.accountId.value.toString(),
                closing_date = statement.closingDate.toEpochDays(),
                starting_balance = statement.startingBalance.value,
                ending_balance = statement.endingBalance.value,
                is_reconciled = statement.isReconciled,
                id = statement.id.id.toString(),
            )
        }
    }

    override suspend fun removeById(statementId: StatementId) {
        withContext(context) {
            database.statementQueries.deleteById(statementId.id.toString())
        }
    }
}

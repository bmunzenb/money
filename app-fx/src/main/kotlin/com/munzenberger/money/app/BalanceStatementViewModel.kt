package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.app.model.getUnreconciled
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Statement
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Task
import java.time.LocalDate

class BalanceStatementViewModel {
    private val operationInProgress = SimpleBooleanProperty(false)

    val statementDateProperty = SimpleObjectProperty<LocalDate>()
    val startingBalanceProperty = SimpleObjectProperty<Money>()
    val endingBalanceProperty = SimpleObjectProperty<Money>()

    val operationInProgressProperty: ReadOnlyBooleanProperty = operationInProgress

    val isInvalidBinding: BooleanBinding =
        statementDateProperty.isNull
            .or(startingBalanceProperty.isNull)
            .or(endingBalanceProperty.isNull)

    private lateinit var database: MoneyDatabase
    private lateinit var statement: Statement

    fun start(
        account: Account,
        database: MoneyDatabase,
        onError: (Throwable) -> Unit,
    ) {
        this.database = database

        val task =
            object : Task<Statement>() {
                override fun call(): Statement {
                    // get the last unreconciled statement, or create a new one
                    return Statement.getUnreconciled(account.identity!!, database) ?: Statement().apply {
                        setAccount(account)
                        isReconciled = false
                    }
                }

                override fun succeeded() {
                    statement = value
                    statementDateProperty.value = statement.closingDate
                    startingBalanceProperty.value = statement.startingBalance
                    endingBalanceProperty.value = statement.endingBalance
                }

                override fun failed() {
                    onError.invoke(exception)
                }
            }

        operationInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }

    fun saveStatement(
        onSuccess: (Statement) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val task =
            object : Task<Statement>() {
                override fun call(): Statement =
                    statement.apply {
                        closingDate = statementDateProperty.value
                        startingBalance = startingBalanceProperty.value
                        endingBalance = endingBalanceProperty.value
                        save(database)
                    }

                override fun succeeded() {
                    onSuccess.invoke(value)
                }

                override fun failed() {
                    onError.invoke(exception)
                }
            }

        operationInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }
}

package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.executeAsync
import com.munzenberger.money.app.model.getUnreconciled
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Statement
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import java.time.LocalDate

class StartBalanceAccountViewModel {

    private val loadStatus = SimpleAsyncStatusProperty()

    val statementDateProperty = SimpleObjectProperty<LocalDate>()
    val statementBalanceProperty = SimpleObjectProperty<Money>()

    val loadStatusProperty: ReadOnlyAsyncStatusProperty = loadStatus

    val isInvalidBinding: BooleanBinding =
            statementDateProperty.isNull
            .or(statementBalanceProperty.isNull)

    private lateinit var statement: Statement

    fun start(account: Account, database: MoneyDatabase) {

        loadStatus.executeAsync {

            // get the last unreconciled statement, or start a new one
            statement = Statement.getUnreconciled(account.identity!!, database) ?: Statement().apply {
                setAccount(account)
                isReconciled = false
            }

            // TODO may not be safe to set property value on background thread, move to UI thread
            statementDateProperty.value = statement.closingDate
            statementBalanceProperty.value = statement.endingBalance
        }
    }

    fun prepareStatement(): Statement = statement.apply {
        closingDate = statementDateProperty.value
        endingBalance = statementBalanceProperty.value
    }
}

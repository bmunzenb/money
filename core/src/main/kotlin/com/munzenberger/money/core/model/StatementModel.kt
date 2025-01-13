package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import java.sql.ResultSet

data class StatementModel(
    var account: Long? = null,
    var closingDate: Long? = null,
    var startingBalance: Long? = null,
    var endingBalance: Long? = null,
    var isReconciled: Boolean? = null,
) : Model()

object StatementTable : Table<StatementModel>() {
    override val tableName = "STATEMENTS"
    override val identityColumn = "STATEMENT_ID"

    const val STATEMENT_ACCOUNT_ID = "STATEMENT_ACCOUNT_ID"
    const val STATEMENT_CLOSING_DATE = "STATEMENT_CLOSING_DATE"
    const val STATEMENT_STARTING_BALANCE = "STATEMENT_STARTING_BALANCE"
    const val STATEMENT_ENDING_BALANCE = "STATEMENT_ENDING_BALANCE"
    const val STATEMENT_IS_RECONCILED = "STATEMENT_IS_RECONCILED"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: StatementModel,
    ) {
        settable.set(STATEMENT_ACCOUNT_ID, model.account)
        settable.set(STATEMENT_CLOSING_DATE, model.closingDate)
        settable.set(STATEMENT_STARTING_BALANCE, model.startingBalance)
        settable.set(STATEMENT_ENDING_BALANCE, model.endingBalance)
        settable.set(STATEMENT_IS_RECONCILED, model.isReconciled)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: StatementModel,
    ): StatementModel =
        model.apply {
            identity = resultSet.getLong(identityColumn)
            account = resultSet.getLong(STATEMENT_ACCOUNT_ID)
            closingDate = resultSet.getLong(STATEMENT_CLOSING_DATE)
            startingBalance = resultSet.getLong(STATEMENT_STARTING_BALANCE)
            endingBalance = resultSet.getLong(STATEMENT_ENDING_BALANCE)
            isReconciled = resultSet.getBoolean(STATEMENT_IS_RECONCILED)
        }
}

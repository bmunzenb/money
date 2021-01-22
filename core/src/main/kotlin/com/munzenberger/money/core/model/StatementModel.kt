package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import java.sql.ResultSet

data class StatementModel(
        var account: Long? = null,
        var closingDate: Long? = null,
        var startingBalance: Long? = null,
        var endingBalance: Long? = null,
        var isReconciled: Boolean? = null
) : Model()

object StatementTable: Table<StatementModel>() {

    override val name = "STATEMENTS"
    override val identityColumn = "STATEMENT_ID"

    const val accountColumn = "STATEMENT_ACCOUNT_ID"
    const val closingDateColumn = "STATEMENT_CLOSING_DATE"
    const val startingBalanceColumn = "STATEMENT_STARTING_BALANCE"
    const val endingBalanceColumn = "STATEMENT_ENDING_BALANCE"
    const val isReconciledColumn = "STATEMENT_IS_RECONCILED"

    override fun setValues(settable: SettableQueryBuilder<*>, model: StatementModel) {
        settable.set(accountColumn, model.account)
        settable.set(closingDateColumn, model.closingDate)
        settable.set(startingBalanceColumn, model.startingBalance)
        settable.set(endingBalanceColumn, model.endingBalance)
        settable.set(isReconciledColumn, model.isReconciled)
    }

    override fun getValues(resultSet: ResultSet, model: StatementModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.account = resultSet.getLong(accountColumn)
        model.closingDate = resultSet.getLong(closingDateColumn)
        model.startingBalance = resultSet.getLong(startingBalanceColumn)
        model.endingBalance = resultSet.getLong(endingBalanceColumn)
        model.isReconciled = resultSet.getBoolean(isReconciledColumn)
    }
}

package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import java.sql.ResultSet

data class PayeeModel(
    var name: String? = null,
) : Model()

object PayeeTable : Table<PayeeModel>() {
    override val tableName = "PAYEES"
    override val identityColumn = "PAYEE_ID"

    const val PAYEE_NAME = "PAYEE_NAME"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: PayeeModel,
    ) {
        settable.set(PAYEE_NAME, model.name)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: PayeeModel,
    ): PayeeModel =
        model.apply {
            identity = resultSet.getLong(identityColumn)
            name = resultSet.getString(PAYEE_NAME)
        }
}

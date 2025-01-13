package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import java.sql.ResultSet

data class BankModel(var name: String? = null) : Model()

object BankTable : Table<BankModel>() {
    override val tableName = "BANKS"
    override val identityColumn = "BANK_ID"

    const val BANK_NAME = "BANK_NAME"

    override fun setValues(
        settable: SettableQueryBuilder<*>,
        model: BankModel,
    ) {
        settable.set(BANK_NAME, model.name)
    }

    override fun getValues(
        resultSet: ResultSet,
        model: BankModel,
    ): BankModel {
        return model.apply {
            identity = resultSet.getLong(identityColumn)
            name = resultSet.getString(BANK_NAME)
        }
    }
}

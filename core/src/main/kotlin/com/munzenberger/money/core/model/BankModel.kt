package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder
import java.sql.ResultSet

data class BankModel(var name: String? = null) : Model()

object BankTable : Table<BankModel>() {

    override val name = "BANKS"
    override val identityColumn = "BANK_ID"

    const val nameColumn = "BANK_NAME"

    override fun setValues(settable: SettableQueryBuilder<*>, model: BankModel) {
        settable.set(nameColumn, model.name)
    }

    override fun getValues(resultSet: ResultSet, model: BankModel) {
        model.identity = resultSet.getLong(identityColumn)
        model.name = resultSet.getString(nameColumn)
    }
}

package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder

data class PayeeModel(var name: String? = null) : Model()

object PayeeTable : Table<PayeeModel>() {

    override val name = "PAYEES"
    override val identityColumn = "PAYEE_ID"

    const val nameColumn = "PAYEE_NAME"

    override fun setValues(settable: SettableQueryBuilder<*>, model: PayeeModel) {
        settable.set(nameColumn, model.name)
    }
}

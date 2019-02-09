package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder

data class PayeeModel(var name: String? = null) : Model()

object PayeeModelQueryBuilder : ModelQueryBuilder<PayeeModel>() {

    override val table = "PAYEES"
    override val identityColumn = "PAYEE_ID"

    const val nameColumn = "PAYEE_NAME"

    override fun setValues(settable: SettableQueryBuilder<*>, model: PayeeModel) {
        settable.set(nameColumn, model.name)
    }
}

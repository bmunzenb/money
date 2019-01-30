package com.munzenberger.money.core.model

import com.munzenberger.money.sql.SettableQueryBuilder

data class BankModel(var name: String? = null) : Model()

object BankRepository : Repository<BankModel>() {

    override val table = "BANKS"

    override val identityColumn = "BANK_ID"

    const val nameColumn = "BANK_NAME"

    override fun setValues(settable: SettableQueryBuilder<*>, model: BankModel) {
        settable.set(nameColumn, model.name)
    }
}

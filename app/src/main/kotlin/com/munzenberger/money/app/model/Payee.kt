package com.munzenberger.money.app.model

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.observableGetAll

fun Payee.Companion.getAllSorted(database: MoneyDatabase) = observableGetAll(database).map {
    it.sortedBy { p -> p.name }
}

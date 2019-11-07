package com.munzenberger.money.app.model

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee

fun Payee.Companion.getAllSorted(database: MoneyDatabase) = getAll(database).map {
    it.sortedBy { p -> p.name }
}

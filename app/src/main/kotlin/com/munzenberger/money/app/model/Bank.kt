package com.munzenberger.money.app.model

import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.MoneyDatabase

fun Bank.Companion.getAllSorted(database: MoneyDatabase) = Bank.getAll(database).map {
    it.sortedBy { b -> b.name }
}
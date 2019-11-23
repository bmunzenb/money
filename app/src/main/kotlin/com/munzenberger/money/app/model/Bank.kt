package com.munzenberger.money.app.model

import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.rx.observableGetAll

fun Bank.Companion.getAllSorted(database: MoneyDatabase) = Bank.observableGetAll(database).map {
    it.sortedBy { b -> b.name }
}
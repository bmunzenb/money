package com.munzenberger.money.app.model

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.sanitize
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.getBalance
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

class FXAccount(
    private val account: Account,
    private val database: MoneyDatabase,
) {
    val identity = account.identity!!

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(account.name)
    val typeProperty: ReadOnlyStringProperty = SimpleStringProperty(account.accountType?.name)
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(account.number?.sanitize())

    private val balance = SimpleAsyncObjectProperty<Money>()
    val balanceProperty: ReadOnlyAsyncObjectProperty<Money> = balance

    fun fetchBalance() {
        balance.setValueAsync { account.getBalance(database) }
    }
}

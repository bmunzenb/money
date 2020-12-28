package com.munzenberger.money.app.model

import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.singleValue
import com.munzenberger.money.app.sanitize
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.getBalance
import io.reactivex.rxjava3.core.Single
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

class FXAccount(private val account: Account, private val database: MoneyDatabase) {

    val identity = account.identity!!

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(account.name)
    val typeProperty: ReadOnlyStringProperty = SimpleStringProperty(account.accountType?.name)
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(account.number?.sanitize())

    private val balance = SimpleAsyncObjectProperty<Money>()
    val balanceProperty: ReadOnlyAsyncObjectProperty<Money> = balance

    val observableBalance: Single<Money>
        get() = balance.singleValue { account.getBalance(database) }
}

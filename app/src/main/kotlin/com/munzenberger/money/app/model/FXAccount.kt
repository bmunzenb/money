package com.munzenberger.money.app.model

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.sanitize
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.core.rx.observableBalance
import io.reactivex.Observable
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class FXAccount(account: Account, database: ObservableMoneyDatabase) {

    val identity = account.identity!!

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(account.name)
    val typeProperty: ReadOnlyObjectProperty<FXAccountType> = SimpleObjectProperty(account.accountType?.let { FXAccountType(it) })
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(account.number?.sanitize())

    private val balance = SimpleAsyncObjectProperty<Money>()
    val balanceProperty: ReadOnlyAsyncObjectProperty<Money> = balance

    val observableBalance: Observable<Money> by lazy {
        account.observableBalance(database)
                .doOnSubscribe { balance.set(AsyncObject.Executing()) }
                .doOnNext { balance.set(AsyncObject.Complete(it)) }
                .doOnError { balance.set(AsyncObject.Error(it)) }
    }
}

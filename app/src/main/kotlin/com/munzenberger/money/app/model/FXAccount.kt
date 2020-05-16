package com.munzenberger.money.app.model

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.sanitize
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.Single
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class FXAccount(account: Account, database: MoneyDatabase) {

    val identity = account.identity!!

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(account.name)
    val typeProperty: ReadOnlyObjectProperty<FXAccountType> = SimpleObjectProperty(account.accountType?.let { FXAccountType(it) })
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(account.number?.sanitize())

    private val balance = SimpleAsyncObjectProperty<Money>()
    val balanceProperty: ReadOnlyAsyncObjectProperty<Money> = balance

    val observableBalance: Single<Money> by lazy {
        Single.fromCallable { account.getBalance(database) }
                .doOnSubscribe { balance.value = AsyncObject.Executing() }
                .doOnError { balance.value = AsyncObject.Error(it) }
                .doOnSuccess { balance.value = AsyncObject.Complete(it) }
    }
}

package com.munzenberger.money.app.model

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class FXAccount(private val account: Account, private val database: MoneyDatabase) {

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(account.name)
    val typeProperty: ReadOnlyObjectProperty<FXAccountType> = SimpleObjectProperty(account.accountType?.let { FXAccountType(it) })
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(account.number)

    private val balance = SimpleAsyncObjectProperty<Long>()
    val balanceProperty: ReadOnlyAsyncObjectProperty<Long>
        get() = balance.apply {
            when {
                value.status == AsyncObject.Status.PENDING -> subscribe(account.balance(database))
            }
        }
}

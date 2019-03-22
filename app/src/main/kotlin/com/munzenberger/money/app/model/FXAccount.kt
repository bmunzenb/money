package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

class FXAccount(account: Account) {

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(account.name)
}

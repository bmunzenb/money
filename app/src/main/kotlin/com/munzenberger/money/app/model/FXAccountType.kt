package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class FXAccountType(accountType: AccountType) {

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(accountType.name)
    val categoryProperty: ReadOnlyObjectProperty<AccountType.Category> = SimpleObjectProperty(accountType.category)
}
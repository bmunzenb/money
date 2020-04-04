package com.munzenberger.money.app.model

import com.munzenberger.money.core.Payee
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.time.LocalDate

class FXPayee(payee: Payee, lastPaid: LocalDate? = null) {

    val nameProperty: ReadOnlyStringProperty = SimpleStringProperty(payee.name)
    val lastPaidProperty: ReadOnlyObjectProperty<LocalDate> = SimpleObjectProperty(lastPaid)
}

package com.munzenberger.money.app

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty

class EditTransfersViewModel {

    private val doneDisabled = SimpleBooleanProperty(true)

    val doneDisabledProperty: ReadOnlyBooleanProperty = doneDisabled

    fun start() {

    }
}

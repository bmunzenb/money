package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import javafx.beans.property.ReadOnlyStringProperty

interface AccountEntriesViewModel {

    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXAccountEntry>>
    val debitTextProperty: ReadOnlyStringProperty
    val creditTextProperty: ReadOnlyStringProperty
}
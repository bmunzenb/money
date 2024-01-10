package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.core.TransactionStatus
import javafx.beans.property.ReadOnlyStringProperty

interface AccountEntriesViewModel {

    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXAccountEntry>>
    val amountTextProperty: ReadOnlyStringProperty

    fun updateEntryStatus(entry: FXAccountEntry, status: TransactionStatus, completionBlock: (Throwable?) -> Unit)
}

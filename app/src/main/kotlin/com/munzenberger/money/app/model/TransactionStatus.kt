package com.munzenberger.money.app.model

import com.munzenberger.money.core.TransactionStatus

val TransactionStatus.displayName: String
    get() = when (this) {
        TransactionStatus.CLEARED -> "Cleared"
        TransactionStatus.RECONCILED -> "Reconciled"
        else -> ""
    }

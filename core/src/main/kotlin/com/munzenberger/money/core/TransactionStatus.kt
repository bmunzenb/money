package com.munzenberger.money.core

enum class TransactionStatus {
    UNRECONCILED, CLEARED, RECONCILED;

    companion object {
        fun parse(string: String?, defaultValue: TransactionStatus = UNRECONCILED) =
                string?.let { valueOf(it) } ?: defaultValue
    }
}

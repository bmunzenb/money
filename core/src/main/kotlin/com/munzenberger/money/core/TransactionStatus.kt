package com.munzenberger.money.core

enum class TransactionStatus(val code: String) {
    UNRECONCILED(""), CLEARED("C"), RECONCILED("R");

    companion object {
        fun fromCode(code: String?) = when (code) {
            CLEARED.code -> CLEARED
            RECONCILED.code -> RECONCILED
            else -> UNRECONCILED
        }
    }
}

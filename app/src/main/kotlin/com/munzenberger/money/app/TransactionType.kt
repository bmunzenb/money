package com.munzenberger.money.app

sealed class TransactionType {

    companion object {
        fun getTypes() = listOf(Credit, Debit)
    }

    abstract val name: String

    object Credit : TransactionType() {
        override val name = "Credit"
    }

    object Debit : TransactionType() {
        override val name = "Debit"
    }

    object Split : TransactionType() {
        override val name = "Split"
    }
}

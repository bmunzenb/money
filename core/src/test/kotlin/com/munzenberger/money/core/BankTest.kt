package com.munzenberger.money.core

class BankTest : PersistableTest<Bank>() {

    override fun createPersistable() = Bank(database).apply {
        name = "Chemical Bank"
    }

    override fun getPersistable(identity: Long) = Bank.get(identity, database)
}

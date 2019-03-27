package com.munzenberger.money.app

import com.munzenberger.money.core.MoneyDatabase

class AccountRegisterViewModel : AutoCloseable {

    fun start(database: MoneyDatabase, accountIdentity: Long) {

    }

    override fun close() {
        // nothing to close
    }
}
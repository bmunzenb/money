package com.munzenberger.money.data.sql

import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector

class SqlMoneyRepositoryConnector : MoneyRepositoryConnector {
    override suspend fun connect(): MoneyRepositoryConnectionStatus {
        TODO()
    }
}

class SqlMigrationStatus : MoneyRepositoryConnectionStatus.RequiresMigration {
    override suspend fun migrate(): MoneyRepositoryConnectionStatus {
        TODO()
    }
}

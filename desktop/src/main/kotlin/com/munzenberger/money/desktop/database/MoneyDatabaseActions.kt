package com.munzenberger.money.desktop.database

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.close
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import java.io.File

suspend fun MoneyRepositoryController.createDatabase(file: File) {
    close()
    if (file.exists()) {
        file.delete()
    }
    handleConnectionStatus(SqlMoneyRepositoryConnector(file).create())
}

suspend fun MoneyRepositoryController.openDatabase(file: File) {
    close()
    handleConnectionStatus(SqlMoneyRepositoryConnector(file).connect())
}

private fun MoneyRepositoryController.handleConnectionStatus(status: MoneyRepositoryConnectionStatus) {
    when (status) {
        is MoneyRepositoryConnectionStatus.Ready -> update(status.moneyRepository)
        is MoneyRepositoryConnectionStatus.Failed -> {
            status.error.printStackTrace()
            TODO("Handle open database error gracefully")
        }
        is MoneyRepositoryConnectionStatus.RequiresMigration -> TODO("Database migrations not yet implemented.")
        MoneyRepositoryConnectionStatus.UnsupportedVersion -> TODO("Database versioning not yet implemented.")
    }
}

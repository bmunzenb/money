package com.munzenberger.money.desktop.database

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.close
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun MoneyRepositoryController.createDatabase(file: File): MoneyRepositoryConnectionStatus =
    withContext(Dispatchers.IO) {
        close()
        if (file.exists()) {
            file.delete()
        }
        SqlMoneyRepositoryConnector(file).create().also { applyConnectionStatus(it) }
    }

suspend fun MoneyRepositoryController.openDatabase(file: File): MoneyRepositoryConnectionStatus =
    withContext(Dispatchers.IO) {
        close()
        SqlMoneyRepositoryConnector(file).connect().also { applyConnectionStatus(it) }
    }

private fun MoneyRepositoryController.applyConnectionStatus(status: MoneyRepositoryConnectionStatus) {
    if (status is MoneyRepositoryConnectionStatus.Ready) {
        update(status.moneyRepository)
    }
}

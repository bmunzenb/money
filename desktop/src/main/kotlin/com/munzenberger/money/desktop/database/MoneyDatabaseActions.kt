package com.munzenberger.money.desktop.database

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.close
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import java.io.File

suspend fun MoneyRepositoryController.createDatabase(file: File): MoneyRepositoryConnectionStatus {
    close()
    if (file.exists()) {
        file.delete()
    }
    return SqlMoneyRepositoryConnector(file).create().also { applyConnectionStatus(it) }
}

suspend fun MoneyRepositoryController.openDatabase(file: File): MoneyRepositoryConnectionStatus {
    close()
    return SqlMoneyRepositoryConnector(file).connect().also { applyConnectionStatus(it) }
}

private fun MoneyRepositoryController.applyConnectionStatus(status: MoneyRepositoryConnectionStatus) {
    if (status is MoneyRepositoryConnectionStatus.Ready) {
        update(status.moneyRepository)
    }
}

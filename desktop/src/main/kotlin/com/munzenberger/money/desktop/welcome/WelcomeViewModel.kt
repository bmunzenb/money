package com.munzenberger.money.desktop.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import kotlinx.coroutines.launch
import java.io.File

class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    fun createDatabase(file: File) {
        if (file.exists()) {
            file.delete()
        }
        viewModelScope.launch {
            handleConnectionStatus(SqlMoneyRepositoryConnector(file).create())
        }
    }

    fun openDatabase(file: File) {
        viewModelScope.launch {
            handleConnectionStatus(SqlMoneyRepositoryConnector(file).connect())
        }
    }

    private suspend fun handleConnectionStatus(status: MoneyRepositoryConnectionStatus) {
        when (status) {
            is MoneyRepositoryConnectionStatus.Ready -> repositoryController.update(status.moneyRepository)
            is MoneyRepositoryConnectionStatus.Failed -> { status.error.printStackTrace() }
            is MoneyRepositoryConnectionStatus.RequiresMigration -> TODO("Database migrations not yet implemented.")
            MoneyRepositoryConnectionStatus.UnsupportedVersion -> TODO("Database versioning not yet implemented.")
        }
    }
}

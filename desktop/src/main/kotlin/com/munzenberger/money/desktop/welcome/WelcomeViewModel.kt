package com.munzenberger.money.desktop.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.desktop.database.createDatabase
import com.munzenberger.money.desktop.database.openDatabase
import kotlinx.coroutines.launch
import java.io.File

class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    fun createDatabase(file: File) {
        viewModelScope.launch {
            repositoryController.createDatabase(file)
        }
    }

    fun openDatabase(file: File) {
        viewModelScope.launch {
            repositoryController.openDatabase(file)
        }
    }
}

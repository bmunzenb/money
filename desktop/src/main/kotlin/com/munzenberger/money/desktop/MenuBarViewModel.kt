package com.munzenberger.money.desktop

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.close

class MenuBarViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    fun onExitSelected() {
        repositoryController.close()
    }
}

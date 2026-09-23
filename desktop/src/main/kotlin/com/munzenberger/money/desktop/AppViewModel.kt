package com.munzenberger.money.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.launch

class AppViewModel(
    private val navigator: Navigator,
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    init {
        viewModelScope.launch {
            repositoryController.moneyRepository.collect { repository ->
                val route = if (repository != null) Route.AccountList else Route.Welcome
                navigator.navigate {
                    clear()
                    add(route)
                }
            }
        }
    }
}

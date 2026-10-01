package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import com.munzenberger.money.desktop.navigation.Navigator

class NewAccountViewModel(
    private val navigator: Navigator
) : ViewModel() {

    fun onBackClick() {
        navigator.navigate { removeLast() }
    }
}

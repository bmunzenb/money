package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals

class NewAccountViewModelTest {

    private val navigator = Navigator()

    @Test
    fun `onBackClick pops the new account route from the back stack`() {
        navigator.navigate { clear(); add(Route.AccountList); add(Route.NewAccount) }
        val viewModel = NewAccountViewModel(navigator)

        viewModel.onBackClick()

        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
        assertEquals(Route.AccountList, navigator.currentRoute.value)
    }
}

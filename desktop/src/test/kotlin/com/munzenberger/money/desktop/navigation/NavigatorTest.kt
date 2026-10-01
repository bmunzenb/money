package com.munzenberger.money.desktop.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {

    private val navigator = Navigator()

    @Test
    fun `backStack starts with the Welcome route`() {
        assertEquals(listOf(Route.Welcome), navigator.backStack.toList())
    }

    @Test
    fun `navigate applies the event to the back stack`() {
        navigator.navigate {
            clear()
            add(Route.AccountList)
        }

        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
    }

    @Test
    fun `currentRoute starts as the Welcome route`() {
        assertEquals(Route.Welcome, navigator.currentRoute.value)
    }

    @Test
    fun `currentRoute tracks the last route on the back stack`() {
        navigator.navigate { add(Route.AccountList) }
        assertEquals(Route.AccountList, navigator.currentRoute.value)

        navigator.navigate { removeAt(lastIndex) }
        assertEquals(Route.Welcome, navigator.currentRoute.value)
    }
}

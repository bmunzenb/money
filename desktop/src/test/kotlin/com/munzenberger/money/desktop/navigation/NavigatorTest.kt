package com.munzenberger.money.desktop.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `canNavigateBack is false with a single back stack entry`() {
        assertFalse(navigator.canNavigateBack.value)
    }

    @Test
    fun `canNavigateBack is true once the back stack has more than one entry`() {
        navigator.navigate { add(Route.AccountList) }

        assertTrue(navigator.canNavigateBack.value)
    }

    @Test
    fun `canNavigateBack is false again once the back stack shrinks to one entry`() {
        navigator.navigate { add(Route.AccountList) }
        navigator.navigate { removeAt(lastIndex) }

        assertFalse(navigator.canNavigateBack.value)
    }
}

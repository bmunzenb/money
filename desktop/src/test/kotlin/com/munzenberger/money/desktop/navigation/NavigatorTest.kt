package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavBackStack
import app.cash.turbine.test
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class NavigatorTest {

    private val navigator = Navigator()

    @Test
    fun `events is a SharedFlow`() {
        assertIs<SharedFlow<NavigationEvent>>(navigator.events)
    }

    @Test
    fun `navigate with event emits to events flow`() = runTest {
        val block: NavBackStack<Route>.() -> Unit = {}
        val event = NavigationEvent(block)

        navigator.events.test {
            navigator.navigate(event)
            assertSame(event, awaitItem())
        }
    }

    @Test
    fun `navigate with block emits event containing block`() = runTest {
        val block: NavBackStack<Route>.() -> Unit = {}

        navigator.events.test {
            navigator.navigate(block)
            assertSame(block, awaitItem().block)
        }
    }
}

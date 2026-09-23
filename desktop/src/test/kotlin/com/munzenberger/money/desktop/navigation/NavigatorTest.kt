package com.munzenberger.money.desktop.navigation

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
        val event: NavigationEvent = {}

        navigator.events.test {
            navigator.navigate(event)
            assertSame(event, awaitItem())
        }
    }
}

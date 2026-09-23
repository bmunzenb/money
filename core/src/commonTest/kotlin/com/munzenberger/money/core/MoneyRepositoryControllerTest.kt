package com.munzenberger.money.core

import com.munzenberger.money.data.api.MoneyRepository
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class MoneyRepositoryControllerTest {

    @Test
    fun testInitialValueIsNull() {
        val controller = MoneyRepositoryController()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testUpdateSetsRepository() {
        val controller = MoneyRepositoryController()
        val repository = mockk<MoneyRepository>()

        controller.update(repository)

        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun testUpdateReplacesExistingRepository() {
        val controller = MoneyRepositoryController()
        val first = mockk<MoneyRepository>()
        val second = mockk<MoneyRepository>()

        controller.update(first)
        controller.update(second)

        assertSame(second, controller.moneyRepository.value)
    }

    @Test
    fun testClearResetsToNull() {
        val controller = MoneyRepositoryController()
        controller.update(mockk<MoneyRepository>())

        controller.clear()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testClearWithoutUpdateStaysNull() {
        val controller = MoneyRepositoryController()

        controller.clear()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testCloseClosesRepository() {
        val controller = MoneyRepositoryController()
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        controller.update(repository)

        controller.close()

        verify(exactly = 1) { repository.close() }
    }

    @Test
    fun testCloseResetsToNull() {
        val controller = MoneyRepositoryController()
        controller.update(mockk<MoneyRepository>(relaxUnitFun = true))

        controller.close()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testCloseWithoutUpdateStaysNull() {
        val controller = MoneyRepositoryController()

        controller.close()

        assertNull(controller.moneyRepository.value)
    }
}

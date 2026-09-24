package com.munzenberger.money.desktop

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class MenuBarViewModelTest {

    private val controller = MoneyRepositoryController()

    @Test
    fun `closes the open repository when exit is selected`() {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        controller.update(repository)

        MenuBarViewModel(controller).onExitSelected()

        verify { repository.close() }
    }

    @Test
    fun `does nothing when exit is selected with no open repository`() {
        MenuBarViewModel(controller).onExitSelected()
    }
}

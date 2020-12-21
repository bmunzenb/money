package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class DelayedCategoryTest {

    @Test
    fun `delayed category entry formats category name`() {

        val category = mockk<Category>().apply {
            every { name } returns "Category"
        }

        val dc = DelayedCategory.Category(category, null)

        assertEquals("Category", dc.name)
    }

    @Test
    fun `delayed category entry formats category name with parent`() {

        val category = mockk<Category>().apply {
            every { name } returns "Category"
        }

        val dc = DelayedCategory.Category(category, "Parent")

        assertEquals("Parent : Category", dc.name)
    }
}

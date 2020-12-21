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

        val dc = DelayedCategory.Entry(category, null)

        assertEquals("Category", dc.name)
    }

    @Test
    fun `delayed category entry formats category name with parent`() {

        val category = mockk<Category>().apply {
            every { name } returns "Category"
        }

        val dc = DelayedCategory.Entry(category, "Parent")

        assertEquals("Parent : Category", dc.name)
    }

    @Test
    fun `delayed category pending formats category name`() {

        val dc = DelayedCategory.Pending("Category")

        assertEquals("Category", dc.name)
    }

    @Test
    fun `delayed category pending formats category name with parent`() {

        val dc = DelayedCategory.Pending("Parent:Category")

        assertEquals("Parent : Category", dc.name)
    }

    @Test
    fun `delayed category pending trims category names`() {

        val dc = DelayedCategory.Pending(" Parent  :  Category ")

        assertEquals("Parent : Category", dc.name)
    }
}

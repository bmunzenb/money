package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionCategoryTest {

    @Test
    fun `transaction category entry formats category name`() {

        val category = mockk<Category>().apply {
            every { name } returns "Category"
        }

        val dc = TransactionCategory.Entry(category, null)

        assertEquals("Category", dc.name)
    }

    @Test
    fun `transaction category entry formats category name with parent`() {

        val category = mockk<Category>().apply {
            every { name } returns "Category"
        }

        val dc = TransactionCategory.Entry(category, "Parent")

        assertEquals("Parent : Category", dc.name)
    }

    @Test
    fun `transaction category pending formats category name`() {

        val dc = TransactionCategory.Pending("Category")

        assertEquals("Category", dc.name)
    }

    @Test
    fun `transaction category pending formats category name with parent`() {

        val dc = TransactionCategory.Pending("Parent:Category")

        assertEquals("Parent : Category", dc.name)
    }

    @Test
    fun `transaction category pending trims category names`() {

        val dc = TransactionCategory.Pending(" Parent  :  Category ")

        assertEquals("Parent : Category", dc.name)
    }
}

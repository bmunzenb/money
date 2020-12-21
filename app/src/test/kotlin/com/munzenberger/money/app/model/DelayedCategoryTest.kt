package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class DelayedCategoryTest {

    @Test
    fun `delayed category entry formats category name`() {

        val c = CategoryWithParent(
               identity = 1,
               name = "Category",
               parentName = null
        )

        val dc = DelayedCategory.Category(c)

        assertEquals("Category", dc.name)
    }

    @Test
    fun `delayed category entry formats category name with parent`() {

        val c = CategoryWithParent(
                identity = 1,
                name = "Category",
                parentName = "Parent"
        )

        val dc = DelayedCategory.Category(c)

        assertEquals("Parent : Category", dc.name)
    }
}

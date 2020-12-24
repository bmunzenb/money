package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import com.munzenberger.money.core.MoneyDatabaseTestSupport
import com.munzenberger.money.core.model.CategoryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryTest : MoneyDatabaseTestSupport() {

    @Test
    fun getAll() {

        val p1 = Category().apply {
            name = "p1"
            type = CategoryType.INCOME
            save(database)
        }

        val cat1 = Category().apply {
            name = "cat1"
            type = CategoryType.INCOME
            setParent(p1)
            save(database)
        }

        val cat2 = Category().apply {
            name = "cat2"
            type = CategoryType.INCOME
            setParent(p1)
            save(database)
        }

        val p2 = Category().apply {
            name = "p2"
            type = CategoryType.EXPENSE
            save(database)
        }

        val cat3 = Category().apply {
            name = "cat3"
            type = CategoryType.EXPENSE
            setParent(p2)
            save(database)
        }

        val cat4 = Category().apply {
            name = "cat4"
            type = CategoryType.EXPENSE
            setParent(p2)
            save(database)
        }

        val categories = Category.getAllWithParent(database)

        assertEquals(6, categories.size)

        assertEquals(p1.identity, categories[0].first.identity)
        assertNull(categories[0].second)

        assertEquals(cat1.identity, categories[1].first.identity)
        assertEquals("p1", categories[1].second)

        assertEquals(cat2.identity, categories[2].first.identity)
        assertEquals("p1", categories[2].second)

        assertEquals(p2.identity, categories[3].first.identity)
        assertNull(categories[3].second)

        assertEquals(cat3.identity, categories[4].first.identity)
        assertEquals("p2", categories[4].second)

        assertEquals(cat4.identity, categories[5].first.identity)
        assertEquals("p2", categories[5].second)
    }
}

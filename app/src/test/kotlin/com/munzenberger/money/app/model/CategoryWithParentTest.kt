package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import com.munzenberger.money.core.MoneyDatabaseTestSupport
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryWithParentTest : MoneyDatabaseTestSupport() {

    @Test
    fun getAll() {

        val p1 = Category().apply {
            name = "p1"
            save(database)
        }

        val cat1 = Category().apply {
            name = "cat1"
            setParent(p1)
            save(database)
        }

        val cat2 = Category().apply {
            name = "cat2"
            setParent(p1)
            save(database)
        }

        val p2 = Category().apply {
            name = "p2"
            save(database)
        }

        val cat3 = Category().apply {
            name = "cat3"
            setParent(p2)
            save(database)
        }

        val cat4 = Category().apply {
            name = "cat4"
            setParent(p2)
            save(database)
        }

        val categories = CategoryWithParent.getAll(database)

        val expectedCategories = listOf(
                CategoryWithParent(
                        identity = p1.identity!!,
                        name = p1.name!!,
                        parentName = null
                ),
                CategoryWithParent(
                        identity = cat1.identity!!,
                        name = cat1.name!!,
                        parentName = p1.name!!
                ),
                CategoryWithParent(
                        identity = cat2.identity!!,
                        name = cat2.name!!,
                        parentName = p1.name!!
                ),
                CategoryWithParent(
                        identity = p2.identity!!,
                        name = p2.name!!,
                        parentName = null
                ),
                CategoryWithParent(
                        identity = cat3.identity!!,
                        name = cat3.name!!,
                        parentName = p2.name!!
                ),
                CategoryWithParent(
                        identity = cat4.identity!!,
                        name = cat4.name!!,
                        parentName = p2.name!!
                )
        )

        assertEquals(expectedCategories, categories)
    }
}

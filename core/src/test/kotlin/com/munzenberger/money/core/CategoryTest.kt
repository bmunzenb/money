package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryTest : MoneyEntityTest<CategoryIdentity, Category>() {

    override fun createPersistable() =
            Category().randomize()

    override fun getPersistable(identity: CategoryIdentity) =
            Category.get(identity, database)

    override fun getAllPersistables() =
            Category.getAll(database)

    override fun updatePersistable(persistable: Category) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Category, p2: Category) {
        assertEquals(p1.name, p2.name)
        assertEquals(p1.type, p2.type)
    }

    override fun createInvalidIdentity() = CategoryIdentity(42L)

    @Test
    fun `can set a parent category`() {

        val parent = Category().apply {
            randomize()
            save(database)
        }

        val category = Category().apply {
            randomize()
            setParent(parent)
            save(database)
        }

        val c1 = Category.get(category.identity!!, database)

        assertEquals(c1!!.parentRef.getIdentity(database), parent.identity)
    }
}

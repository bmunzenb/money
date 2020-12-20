package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryTest : PersistableTest<Category>() {

    override fun createPersistable() =
            Category().randomize()

    override fun getPersistable(identity: Long) =
            Category.get(identity, database)

    override fun getAllPersistables() =
            Category.getAll(database)

    override fun updatePersistable(persistable: Category) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Category, p2: Category) {
        assertEquals(p1.name, p2.name)
    }

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

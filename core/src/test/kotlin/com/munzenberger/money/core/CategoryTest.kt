package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class CategoryTest : PersistableTest<Category>() {

    override fun createPersistable() = Category(database).randomize(database)

    override fun getPersistable(identity: Long) = Category.get(identity, database)

    override fun getAllPersistables() = Category.getAll(database)

    override fun updatePersistable(persistable: Category) {
        persistable.randomize(database)
    }

    override fun assertPersistablePropertiesAreEquals(p1: Category, p2: Category) {
        assertEquals(p1.name, p2.name)
        assertEquals(p1.account?.identity, p2.account?.identity)
        assertEquals(p1.account?.accountType?.identity, p2.account?.accountType?.identity)
    }
}
package com.munzenberger.money.core

import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Test

abstract class PersistableTest<P : Persistable<*>> : MoneyDatabaseTestSupport() {

    abstract fun createPersistable(): P

    abstract fun getPersistable(identity: Long): Single<P>

    abstract fun getAllPersistables(): Single<List<P>>

    abstract fun updatePersistable(persistable : P)

    abstract fun assertPersistablePropertiesAreEquals(p1: P, p2: P)

    @Test
    fun `retrieve invalid identity emits error`() {

        getPersistable(42L)
                .test()
                .assertError(PersistableNotFoundException::class.java)
    }

    @Test
    fun `can delete an unsaved persistable`() {

        val p = createPersistable().apply { save(database).test().assertComplete() }

        p.delete(database).test().assertComplete()
    }

    @Test
    fun `can store and retrieve a persistable by identity`() {

        val p = createPersistable()
        assertNull(p.identity)

        p.save(database).test().assertComplete()
        assertNotNull(p.identity)

        getPersistable(p.identity!!).test().assertComplete().apply {
            assertValue { it.identity == p.identity }
            assertPersistablePropertiesAreEquals(p, values().first())
        }
    }

    @Test
    open fun `can store and retrieve a list of persistables`() {

        val list = listOf(createPersistable(), createPersistable(), createPersistable())

        list.forEach {
            it.save(database).test().assertComplete()
        }

        getAllPersistables().test().assertComplete().apply {
            assertValue { it.size == list.size }
            values().first().zip(list).forEach {
                assertEquals(it.first.identity, it.second.identity)
                assertPersistablePropertiesAreEquals(it.first, it.second)
            }
        }
    }

    @Test
    fun `can store and delete a persistable`() {

        val p = createPersistable().apply { save(database).test().assertComplete() }
        val identity = p.identity!!

        p.delete(database).test().assertComplete()
        assertNull(p.identity)

        getPersistable(identity)
                .test()
                .assertError(PersistableNotFoundException::class.java)
    }

    @Test
    fun `can store and update a persistable`() {

        val p = createPersistable().apply { save(database).test().assertComplete() }

        updatePersistable(p)

        p.save(database).test().assertComplete()

        getPersistable(p.identity!!).test().assertComplete().apply {
            assertValue { it.identity == p.identity }
            assertPersistablePropertiesAreEquals(p, values().first())
        }
    }
}

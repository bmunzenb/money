package com.munzenberger.money.core

import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Test

abstract class PersistableTest<P : Persistable<*>> : MoneyDatabaseTestSupport() {

    abstract fun createPersistable(): P

    abstract fun getPersistable(identity: Long): Single<P>

    @Test
    fun `save and retrieve persistable`() {

        val p = createPersistable()
        assertNull(p.identity)

        val test = p.save().test()

        test.assertComplete()
        assertNotNull(p.identity)

        val test2 = getPersistable(p.identity!!).test()

        test2.assertComplete()
        test2.assertValue { it.identity == p.identity }
    }

    @Test
    fun `retrieve invalid identity emits error`() {

        val test = getPersistable(42L).test()

        test.assertError(KotlinNullPointerException::class.java)
    }
}

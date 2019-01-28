package com.munzenberger.money.version

import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class VersionManagerTest {

    @Test
    fun `validate mismatched versions`() {

        val v1 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(1)
        }

        val v2 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(2)
        }

        val result = VersionManager.validate(listOf(v1), listOf(v2))

        assertFalse(result)
    }

    @Test
    fun `validate too many versions applied`() {

        val v1 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(1)
        }

        val v2 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(2)
        }

        val result = VersionManager.validate(listOf(v1, v2), listOf(v1))

        assertFalse(result)
    }

    @Test
    fun `validate up to date`() {

        val v1 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(1)
        }

        val v2 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(2)
        }

        val result = VersionManager.validate(listOf(v1, v2), listOf(v1, v2))

        assertTrue(result)
    }

    @Test
    fun `validate needs upgrade`() {

        val v1 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(1)
        }

        val v2 = mock(Version::class.java).apply {
            `when`(hash).thenReturn(2)
        }

        val result = VersionManager.validate(listOf(v1), listOf(v1, v2))

        assertTrue(result)
    }
}

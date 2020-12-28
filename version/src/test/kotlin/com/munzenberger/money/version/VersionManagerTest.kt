package com.munzenberger.money.version

import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class VersionManagerTest {

    private class TestableVersion(override val versionId: Long) : ApplicableVersion<Any> {
        override fun apply(obj: Any) {
            // do nothing
        }
    }

    private class TestableVersionManager(
            val applicable: List<ApplicableVersion<Any>>,
            val applied: List<Version>
    ) : VersionManager<Any>() {

        override fun getApplicableVersions() = applicable

        override fun getAppliedVersions(obj: Any) = applied

        override fun onVersionApplied(obj: Any, version: Version) {
            // do nothing
        }
    }

    @Test
    fun `get version status with no versions applied or applicable returns Current`() {

        val vm = TestableVersionManager(applicable = emptyList(), applied = emptyList())

        assertEquals(CurrentVersion, vm.getVersionStatus(Unit))
    }

    @Test
    fun `get version status with versions applied and applicable returns Current`() {

        val versions = listOf(TestableVersion(1L))

        val vm = TestableVersionManager(applicable = versions, applied = versions)

        assertEquals(CurrentVersion, vm.getVersionStatus(Unit))
    }

    @Test
    fun `get version status with applied and no applicable versions returns Unsupported`() {

        val applied = listOf(TestableVersion(1L))

        val vm = TestableVersionManager(applicable = emptyList(), applied = applied)

        assertEquals(UnsupportedVersion, vm.getVersionStatus(Unit))
    }

    @Test
    fun `get version status with applied and different applicable versions returns Unsupported`() {

        val applied = listOf(TestableVersion(1L))
        val applicable = listOf(TestableVersion(2L))

        val vm = TestableVersionManager(applicable = applicable, applied = applied)

        assertEquals(UnsupportedVersion, vm.getVersionStatus(Unit))
    }

    @Test
    fun `get version status with no applied and applicable versions returns Pending with first flag`() {

        val applicable = listOf(TestableVersion(1L))

        val vm = TestableVersionManager(applicable = applicable, applied = emptyList())

        val status = vm.getVersionStatus(Unit)

        assertTrue(status is PendingUpgrades && status.isFirstUse)
    }

    @Test
    fun `get version status with applied and more applicable versions returns Pending without first flag`() {

        val applied = listOf(TestableVersion(1L))
        val applicable = applied + TestableVersion(2L)

        val vm = TestableVersionManager(applicable = applicable, applied = applied)

        val status = vm.getVersionStatus(Unit)

        assertTrue(status is PendingUpgrades && !status.isFirstUse)
    }

    @Test
    fun `applying pending versions calls onVersionApplied for each version`() {

        val version1 = TestableVersion(1L)
        val version2 = TestableVersion(2L)

        val versions = listOf(version1, version2)

        val obj = mockk<Any>()

        val vm = TestableVersionManager(applicable = versions, applied = emptyList())

        val spy = spyk(vm, recordPrivateCalls = true)

        when (val status = spy.getVersionStatus(obj)) {

            is PendingUpgrades -> {
                status.apply()
                verify {
                    spy["onVersionApplied"](obj, version1)
                    spy["onVersionApplied"](obj, version2)
                }
            }

            else -> fail("status should have been PendingUpdgrades")
        }
    }
}

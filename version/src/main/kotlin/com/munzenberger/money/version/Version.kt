package com.munzenberger.money.version

interface Version {
    val hash: Int
}

interface Versionable {
    fun getAppliedVersions(): List<Version>
}

interface PendingVersion<T : Versionable> : Version {
    fun apply(obj: T)
}

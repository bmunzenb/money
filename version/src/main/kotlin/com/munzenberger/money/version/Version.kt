package com.munzenberger.money.version

interface Version {
    val versionId: Long
}

interface ApplicableVersion<T> : Version {
    fun apply(obj: T)
}

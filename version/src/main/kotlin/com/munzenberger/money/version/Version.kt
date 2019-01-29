package com.munzenberger.money.version

interface Version {
    val hash: Long
}

interface ApplicableVersion<T> : Version {
    fun apply(obj: T)
}

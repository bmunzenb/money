package com.munzenberger.money

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
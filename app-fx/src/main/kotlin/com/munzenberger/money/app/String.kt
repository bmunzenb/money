package com.munzenberger.money.app

fun String.sanitize() =
    when {
        length > 8 -> sanitize(4)
        length > 6 -> sanitize(3)
        else -> sanitize(2)
    }

fun String.sanitize(
    last: Int,
    replacement: Char = 'X',
): String {
    val buf = StringBuffer(this)
    when {
        last < length ->
            (0 until length - last).forEach {
                buf.setCharAt(it, replacement)
            }
    }
    return buf.toString()
}

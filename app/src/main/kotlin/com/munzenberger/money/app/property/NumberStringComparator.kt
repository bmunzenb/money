package com.munzenberger.money.app.property

object NumberStringComparator : Comparator<String?> {
    override fun compare(
        o1: String?,
        o2: String?,
    ): Int {
        if (o1 == null) {
            return 1
        }

        if (o2 == null) {
            return -1
        }

        val n1 = o1.toIntOrNull()
        val n2 = o2.toIntOrNull()

        if (n1 != null && n2 != null) {
            return n1.compareTo(n2)
        }

        if (n1 == null && n2 == null) {
            return o1.compareTo(o2)
        }

        if (n1 == null) {
            return 1
        }

        return -1
    }
}

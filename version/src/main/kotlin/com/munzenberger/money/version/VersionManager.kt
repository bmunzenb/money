package com.munzenberger.money.version

object VersionManager {

    internal fun validate(applied: List<Version>, versions: List<Version>): Boolean {

        val iter1 = applied.iterator()
        val iter2 = versions.iterator()

        // the versions must have been applied in the correct sequence
        while (iter1.hasNext() && iter2.hasNext()) {

            // their hashes must match exactly
            if (iter1.next().hash != iter2.next().hash) {
                return false
            }
        }

        // and there can't be any additional versions not in the master list
        return !iter1.hasNext()
    }
}

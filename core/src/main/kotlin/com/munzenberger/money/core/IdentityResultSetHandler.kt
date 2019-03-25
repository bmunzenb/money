package com.munzenberger.money.core

import com.munzenberger.money.sql.ResultSetHandler
import java.sql.ResultSet

class IdentityResultSetHandler : ResultSetHandler {

    private var mutableIdentity: Long? = null

    val identity: Long
        get() = mutableIdentity!!

    override fun accept(resultSet: ResultSet) {

        if (resultSet.next()) {
            mutableIdentity = resultSet.getLong(1)
        }
    }
}

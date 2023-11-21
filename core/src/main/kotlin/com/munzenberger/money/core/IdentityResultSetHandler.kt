package com.munzenberger.money.core

import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class IdentityResultSetHandler : ResultSetHandler, ResultSetMapper<Long> {

    private var mutableIdentity: Long? = null

    val identity: Long?
        get() = mutableIdentity

    override fun accept(resultSet: ResultSet) {
        if (resultSet.next()) {
            mutableIdentity = apply(resultSet)
        }
    }

    override fun apply(resultSet: ResultSet): Long {
        return resultSet.getLong(1)
    }
}

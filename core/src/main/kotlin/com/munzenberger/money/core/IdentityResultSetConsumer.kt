package com.munzenberger.money.core

import com.munzenberger.money.sql.ResultSetConsumer
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

internal class IdentityResultSetConsumer :
    ResultSetConsumer,
    ResultSetMapper<Long> {
    private var mutableIdentity: Long? = null

    val identity: Long?
        get() = mutableIdentity

    override fun accept(resultSet: ResultSet) {
        if (resultSet.next()) {
            mutableIdentity = apply(resultSet)
        }
    }

    override fun apply(resultSet: ResultSet): Long = resultSet.getLong(1)
}

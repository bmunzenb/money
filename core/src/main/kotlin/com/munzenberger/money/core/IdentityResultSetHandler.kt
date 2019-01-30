package com.munzenberger.money.core

import com.munzenberger.money.sql.FirstResultSetHandler
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

private object IdentityResultSetMapper : ResultSetMapper<Long> {
    override fun map(resultSet: ResultSet) = resultSet.getLong(1)
}

class IdentityResultSetHandler : FirstResultSetHandler<Long>(IdentityResultSetMapper)
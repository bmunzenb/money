package com.munzenberger.money.core.version

import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.version.Version
import java.sql.ResultSet

class VersionResultSetMapper(private val hashColumnName: String) : ResultSetMapper<Version> {

    override fun map(resultSet: ResultSet): Version {

        val hash = resultSet.getLong(hashColumnName)

        return object : Version {
            override val hash = hash
        }
    }
}

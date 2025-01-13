package com.munzenberger.money.core.version

import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.version.Version
import java.sql.ResultSet

class VersionResultSetMapper(private val versionColumnName: String) : ResultSetMapper<Version> {
    override fun apply(resultSet: ResultSet): Version {
        val version = resultSet.getLong(versionColumnName)

        return object : Version {
            override val versionId = version
        }
    }
}

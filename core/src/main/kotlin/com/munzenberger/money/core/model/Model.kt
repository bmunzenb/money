package com.munzenberger.money.core.model

import com.munzenberger.money.sql.OrderableQueryBuilder
import com.munzenberger.money.sql.SelectQueryBuilder
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.deleteQuery
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.insertQuery
import com.munzenberger.money.sql.selectQuery
import com.munzenberger.money.sql.updateQuery
import java.sql.ResultSet

abstract class Model(
    var identity: Long? = null,
)

abstract class Table<M : Model> {
    abstract val tableName: String

    // TODO consider creating a Column data class for all columns in Tables
    abstract val identityColumn: String

    protected abstract fun setValues(
        settable: SettableQueryBuilder<*>,
        model: M,
    )

    internal abstract fun getValues(
        resultSet: ResultSet,
        model: M,
    ): M

    protected open fun applyJoins(select: SelectQueryBuilder) {}

    internal fun select(block: OrderableQueryBuilder<*>.() -> Unit = {}) =
        selectQuery(tableName) {
            applyJoins(this)
            block()
        }

    internal fun select(identity: Long) =
        selectQuery(tableName) {
            applyJoins(this)
            where(identityColumn.eq(identity))
        }

    internal fun insert(model: M) =
        insertQuery(tableName) {
            setValues(this, model)
        }

    internal fun update(
        identity: Long,
        model: M,
    ) = updateQuery(tableName) {
        setValues(this, model)
        where(identityColumn.eq(identity))
    }

    internal fun delete(identity: Long) =
        deleteQuery(tableName) {
            where(identityColumn.eq(identity))
        }

    protected fun SelectQueryBuilder.leftJoin(
        leftColumn: String,
        right: Table<*>,
    ) = leftJoin(tableName, leftColumn, right.tableName, right.identityColumn).apply { right.applyJoins(this) }
}

fun SelectQueryBuilder.innerJoin(
    leftTable: Table<*>,
    leftColumn: String,
    rightTable: Table<*>,
    rightColumn: String,
) = innerJoin(leftTable.tableName, leftColumn, rightTable.tableName, rightColumn)

fun SelectQueryBuilder.leftJoin(
    leftTable: Table<*>,
    leftColumn: String,
    rightTable: Table<*>,
    rightColumn: String,
) = leftJoin(leftTable.tableName, leftColumn, rightTable.tableName, rightColumn)

fun SelectQueryBuilder.rightJoin(
    leftTable: Table<*>,
    leftColumn: String,
    rightTable: Table<*>,
    rightColumn: String,
) = rightJoin(leftTable.tableName, leftColumn, rightTable.tableName, rightColumn)

package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryModel
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.core.model.CategoryType
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

data class CategoryIdentity(override val value: Long) : Identity

class Category internal constructor(model: CategoryModel) : AbstractMoneyEntity<CategoryIdentity, CategoryModel>(model, CategoryTable) {

    constructor() : this(CategoryModel())

    override val identity: CategoryIdentity?
        get() = model.identity?.let { CategoryIdentity(it) }

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    internal val parentRef = PersistableIdentityReference(model.parent?.let { CategoryIdentity(it) })

    fun setParent(category: Category?) {
        parentRef.set(category)
    }

    var type: CategoryType?
        get() = model.type
        set(value) { model.type = value }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.parent = parentRef.getIdentity(tx)?.value
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, CategoryTable, CategoryResultSetMapper())

        fun get(identity: CategoryIdentity, executor: QueryExecutor) =
                get(identity, executor, CategoryTable, CategoryResultSetMapper())
    }
}

class CategoryResultSetMapper : ResultSetMapper<Category> {

    override fun apply(resultSet: ResultSet): Category {

        val model = CategoryModel().apply {
            CategoryTable.getValues(resultSet, this)
        }

        return Category(model)
    }
}

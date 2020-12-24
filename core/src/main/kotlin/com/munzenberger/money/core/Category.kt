package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryModel
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.core.model.CategoryType
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

class Category internal constructor(model: CategoryModel) : Persistable<CategoryModel>(model, CategoryTable) {

    constructor() : this(CategoryModel())

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    internal val parentRef = PersistableIdentityReference(model.parent)

    fun setParent(category: Category?) {
        parentRef.set(category)
    }

    var type: CategoryType?
        get() = model.type
        set(value) { model.type = value }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.parent = parentRef.getIdentity(tx)
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, CategoryTable, CategoryResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
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

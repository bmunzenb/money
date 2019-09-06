package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryModel
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Category internal constructor(model: CategoryModel) : Persistable<CategoryModel>(model, CategoryTable) {

    constructor() : this(CategoryModel())

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    var account: Account? = null

    override fun save(executor: QueryExecutor): Completable {

        val tx = executor.createTransaction()

        val getAccountIdentity = getIdentity(account, tx) { model.account = it }

        return Completable.concatArray(getAccountIdentity, super.save(tx)).withTransaction(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, CategoryTable, CategoryResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, CategoryTable, CategoryResultSetMapper(), Category::class)
    }
}

class CategoryResultSetMapper : ResultSetMapper<Category> {

    override fun apply(resultSet: ResultSet): Category {

        val model = CategoryModel().apply {
            identity = resultSet.getLong(CategoryTable.identityColumn)
            account = resultSet.getLongOrNull(CategoryTable.accountColumn)
            name = resultSet.getString(CategoryTable.nameColumn)
        }

        return Category(model).apply {
            account = model.account?.let { AccountResultSetMapper().apply(resultSet) }
        }
    }
}

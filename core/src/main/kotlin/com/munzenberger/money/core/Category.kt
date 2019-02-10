package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryModel
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Category(executor: QueryExecutor, model: CategoryModel = CategoryModel()) : Persistable<CategoryModel>(model, CategoryTable, executor) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    var account: Account? = null

    override fun save(): Completable {

        val accountIdentity = Persistable.getIdentity(account) { model.account = it }
        return accountIdentity.andThen(super.save())
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, CategoryTable, CategoryResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, CategoryTable, CategoryResultSetMapper(executor), Category::class)
    }
}

class CategoryResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<Category> {

    override fun map(resultSet: ResultSet): Category {

        val model = CategoryModel().apply {
            identity = resultSet.getLong(CategoryTable.identityColumn)
            account = resultSet.getLongOrNull(CategoryTable.accountColumn)
            name = resultSet.getString(CategoryTable.nameColumn)
        }

        return Category(executor, model).apply {
            account = model.account?.let { AccountResultSetMapper(executor).map(resultSet) }
        }
    }
}

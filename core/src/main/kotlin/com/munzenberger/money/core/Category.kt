package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryModel
import com.munzenberger.money.core.model.CategoryModelQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Category(executor: QueryExecutor, model: CategoryModel = CategoryModel()) : Persistable<CategoryModel>(model, CategoryModelQueryBuilder, executor) {

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
                Persistable.getAll(executor, CategoryModelQueryBuilder, CategoryResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, CategoryModelQueryBuilder, CategoryResultSetMapper(executor), Category::class)
    }
}

class CategoryResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<Category> {

    private val accountMapper = AccountResultSetMapper(executor)

    override fun map(resultSet: ResultSet): Category {

        val model = CategoryModel().apply {
            identity = resultSet.getLong(CategoryModelQueryBuilder.identityColumn)
            account = resultSet.getLongOrNull(CategoryModelQueryBuilder.accountColumn)
            name = resultSet.getString(CategoryModelQueryBuilder.nameColumn)
        }

        return Category(executor, model).apply {
            account = model.account?.let { accountMapper.map(resultSet) }
        }
    }
}

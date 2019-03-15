package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryModel
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Category(model: CategoryModel = CategoryModel()) : Persistable<CategoryModel>(model, CategoryTable) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    var account: Account? = null

    override fun save(executor: QueryExecutor): Completable {

        val accountIdentity = Persistable.getIdentity(account, executor) { model.account = it }

        return completableChain(accountIdentity, super.save(executor))
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, CategoryTable, CategoryResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, CategoryTable, CategoryResultSetMapper(), Category::class)
    }
}

class CategoryResultSetMapper : ResultSetMapper<Category> {

    override fun map(resultSet: ResultSet): Category {

        val model = CategoryModel().apply {
            identity = resultSet.getLong(CategoryTable.identityColumn)
            account = resultSet.getLongOrNull(CategoryTable.accountColumn)
            name = resultSet.getString(CategoryTable.nameColumn)
        }

        return Category(model).apply {
            account = model.account?.let { AccountResultSetMapper().map(resultSet) }
        }
    }
}

package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Category
import com.munzenberger.money.core.PersistableNotFoundException
import io.reactivex.Observable

fun Category.Companion.observableCategory(identity: Long, database: ObservableMoneyDatabase): Observable<Category> =
        database.onUpdate.flatMap { Observable.create<Category> {
            when (val value = get(identity, database)) {
                null -> it.onError(PersistableNotFoundException(Category::class, identity))
                else -> it.onNext(value)
            }
        } }

fun Category.Companion.observableCategoryList(database: ObservableMoneyDatabase): Observable<List<Category>> =
        database.onUpdate.flatMap { Observable.fromCallable { getAll(database) } }

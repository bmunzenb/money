package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Persistable
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Completable

fun Persistable<*>.observableSave(executor: QueryExecutor) = Completable.fromAction { save(executor) }

fun Persistable<*>.observableDelete(executor: QueryExecutor) = Completable.fromAction { delete(executor) }
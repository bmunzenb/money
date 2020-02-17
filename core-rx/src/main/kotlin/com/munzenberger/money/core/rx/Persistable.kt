package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Persistable
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

fun Persistable<*>.observableSave(executor: QueryExecutor) = Completable.fromAction { save(executor) }

fun Persistable<*>.observableDelete(executor: QueryExecutor) = Completable.fromAction { delete(executor) }

fun <T : Persistable<*>, R : Comparable<R>> Observable<List<T>>.sortedBy(selector: (T) -> R?): Observable<List<T>> =
        map { it.sortedBy(selector) }

fun <T : Persistable<*>, R : Comparable<R>> Single<List<T>>.sortedBy(selector: (T) -> R?): Single<List<T>> =
        map { it.sortedBy(selector) }

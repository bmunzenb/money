package com.munzenberger.money.app.database

import com.munzenberger.money.app.SchedulerProvider
import com.munzenberger.money.core.ConnectionMoneyDatabase
import com.munzenberger.money.core.DatabaseDialect
import com.munzenberger.money.core.SQLiteDatabaseDialect
import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.CurrentVersion
import com.munzenberger.money.version.PendingUpgrades
import com.munzenberger.money.version.UnsupportedVersion
import com.munzenberger.money.version.VersionStatus
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.sql.DriverManager

interface DatabaseConnectorCallbacks {
    fun onCanceled()
    fun onUnsupportedVersion()
    fun onPendingUpgrades(): Boolean
    fun onConnected(database: ObservableMoneyDatabase)
    fun onConnectError(error: Throwable)
}

abstract class DatabaseConnector {

    abstract fun connect(callbacks: DatabaseConnectorCallbacks)

    protected fun connect(
            name: String,
            driver: String,
            dialect: DatabaseDialect,
            connectionUrl: String,
            user: String? = null,
            password: String? = null,
            callbacks: DatabaseConnectorCallbacks
    ) {

        Single.fromCallable {

                    Class.forName(driver)

                    val connection = DriverManager.getConnection(connectionUrl, user, password)

                    ObservableMoneyDatabase(ConnectionMoneyDatabase(name, dialect, connection)).also {
                        when (dialect) {
                            SQLiteDatabaseDialect ->
                                // SQLite requires explicitly enabling foreign key constraints
                                // https://www.sqlite.org/foreignkeys.html#fk_enable
                                it.execute(Query("PRAGMA foreign_keys = ON"))
                        }
                    }
                }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe({ onConnectSuccess(it, callbacks) }, { callbacks.onConnectError(it) })
    }

    private fun onConnectSuccess(database: ObservableMoneyDatabase, callbacks: DatabaseConnectorCallbacks) {

        Single.fromCallable { MoneyCoreVersionManager().getVersionStatus(database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnError { database.close() }
                .subscribe({ onVersionStatus(database, it, callbacks) }, { callbacks.onConnectError(it) })
    }

    private fun onVersionStatus(database: ObservableMoneyDatabase, status: VersionStatus, callbacks: DatabaseConnectorCallbacks) {

        when (status) {

            is CurrentVersion ->
                callbacks.onConnected(database)

            is UnsupportedVersion -> {
                database.close()
                callbacks.onUnsupportedVersion()
            }

            is PendingUpgrades ->
                when (callbacks.onPendingUpgrades()) {
                    true -> applyPendingUpgrades(database, status, callbacks)
                    else -> {
                        database.close()
                        callbacks.onCanceled()
                    }
                }
        }
    }

    private fun applyPendingUpgrades(database: ObservableMoneyDatabase, upgrades: PendingUpgrades, callbacks: DatabaseConnectorCallbacks) {

        Completable.fromRunnable { upgrades.apply() }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnError { database.close() }
                .subscribe({ callbacks.onConnected(database) }, { callbacks.onConnectError(it) })
    }
}

package com.munzenberger.money.app.database

import com.munzenberger.money.app.SchedulerProvider
import com.munzenberger.money.core.ConnectionMoneyDatabase
import com.munzenberger.money.core.DatabaseDialect
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.CurrentVersion
import com.munzenberger.money.version.PendingUpgrades
import com.munzenberger.money.version.UnsupportedVersion
import com.munzenberger.money.version.VersionStatus
import io.reactivex.Completable
import io.reactivex.Single
import java.sql.DriverManager

typealias DatabaseConnectionHandler = (ObservableMoneyDatabase?) -> Unit

abstract class DatabaseConnector {

    protected fun connect(
            name: String,
            driver: String,
            dialect: DatabaseDialect,
            connectionUrl: String,
            user: String? = null,
            password: String? = null,
            complete: DatabaseConnectionHandler
    ) {

        Single.fromCallable {

                    Class.forName(driver)

                    val connection = DriverManager.getConnection(connectionUrl, user, password)

                    ObservableMoneyDatabase(ConnectionMoneyDatabase(name, dialect, connection)).also {
                        when (driver) {
                            "org.sqlite.JDBC" ->
                                // SQLite requires explicitly enabling foreign key constraints
                                // https://www.sqlite.org/foreignkeys.html#fk_enable
                                it.execute(Query("PRAGMA foreign_keys = ON"))
                        }
                    }
                }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe({ onConnectSuccess(it, complete) }, { onConnectError(it); complete.invoke(null) })
    }

    private fun onConnectSuccess(database: ObservableMoneyDatabase, complete: DatabaseConnectionHandler) {

        Single.fromCallable { MoneyCoreVersionManager().getVersionStatus(database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnError { database.close() }
                .subscribe({ onVersionStatus(database, it, complete) }, { onConnectError(it); complete.invoke(null) })
    }

    private fun onVersionStatus(database: ObservableMoneyDatabase, status: VersionStatus, complete: DatabaseConnectionHandler) {

        when (status) {

            is CurrentVersion -> complete.invoke(database)

            is UnsupportedVersion -> {
                database.close()
                onUnsupportedVersion()
            }

            is PendingUpgrades ->
                if (onPendingUpgrades()) applyPendingUpgrades(database, status, complete)
                else database.close()
        }
    }

    private fun applyPendingUpgrades(database: ObservableMoneyDatabase, upgrades: PendingUpgrades, complete: DatabaseConnectionHandler) {

        Completable.fromRunnable { upgrades.apply() }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnError { database.close() }
                .subscribe({ complete.invoke(database) }, { onConnectError(it); complete.invoke(null) })
    }

    protected abstract fun onUnsupportedVersion()

    protected abstract fun onPendingUpgrades(): Boolean

    protected abstract fun onConnectError(error: Throwable)
}

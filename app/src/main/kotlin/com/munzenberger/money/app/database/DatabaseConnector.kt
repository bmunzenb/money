package com.munzenberger.money.app.database

import com.munzenberger.money.core.ConnectionMoneyDatabase
import com.munzenberger.money.core.DatabaseDialect
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.CurrentVersion
import com.munzenberger.money.version.PendingUpgrades
import com.munzenberger.money.version.UnsupportedVersion
import com.munzenberger.money.version.VersionStatus
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.sql.DriverManager

typealias DatabaseConnectionHandler = (MoneyDatabase?) -> Unit

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

        Single.create<MoneyDatabase> {

            Class.forName(driver)

            val connection = DriverManager.getConnection(connectionUrl, user, password)
            val database = ConnectionMoneyDatabase(name, dialect, connection)

            when (driver) {
                "org.sqlite.JDBC" ->
                    // SQLite requires explicitly enabling foreign key constraints
                    // https://www.sqlite.org/foreignkeys.html#fk_enable
                    database.execute(Query("PRAGMA foreign_keys = ON"))
            }

            it.onSuccess(database)
        }
                .subscribeOn(Schedulers.single())
                .observeOn(JavaFxScheduler.platform())
                .subscribe({ onConnectSuccess(it, complete) }, { onConnectError(it); complete.invoke(null) })
    }

    private fun onConnectSuccess(database: MoneyDatabase, complete: DatabaseConnectionHandler) {

        Single.fromCallable { MoneyCoreVersionManager().getVersionStatus(database) }
                .subscribeOn(Schedulers.single())
                .observeOn(JavaFxScheduler.platform())
                .doOnError { database.close() }
                .subscribe({ onVersionStatus(database, it, complete) }, { onConnectError(it); complete.invoke(null) })
    }

    private fun onVersionStatus(database: MoneyDatabase, status: VersionStatus, complete: DatabaseConnectionHandler) {

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

    private fun applyPendingUpgrades(database: MoneyDatabase, upgrades: PendingUpgrades, complete: DatabaseConnectionHandler) {

        Completable.fromRunnable { upgrades.apply() }
                .subscribeOn(Schedulers.single())
                .observeOn(JavaFxScheduler.platform())
                .doOnError { database.close() }
                .subscribe({ complete.invoke(database) }, { onConnectError(it); complete.invoke(null) })
    }

    protected abstract fun onUnsupportedVersion()

    protected abstract fun onPendingUpgrades(): Boolean

    protected abstract fun onConnectError(error: Throwable)
}

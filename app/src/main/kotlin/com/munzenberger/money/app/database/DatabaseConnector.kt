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

object DatabaseConnector {

    interface Callback {

        fun onConnectSuccess(database: MoneyDatabase)
        fun onConnectPendingUpgrades(): Boolean
        fun onConnectUnsupportedVersion()
        fun onConnectError(error: Throwable)
    }

    fun connect(
            name: String,
            driver: String,
            dialect: DatabaseDialect,
            connectionUrl: String,
            user: String? = null,
            password: String? = null,
            callback: Callback
    ) {

        Single.create<Pair<MoneyDatabase, VersionStatus>> {

            Class.forName(driver)

            val connection = DriverManager.getConnection(connectionUrl, user, password)
            val database = ConnectionMoneyDatabase(name, dialect, connection)

            try {
                when (driver) {
                    "org.sqlite.JDBC" ->
                        // SQLite requires explicitly enabling foreign key constraints
                        // https://www.sqlite.org/foreignkeys.html#fk_enable
                        database.execute(Query("PRAGMA foreign_keys = ON"))
                }

                val status = MoneyCoreVersionManager().getVersionStatus(database)
                it.onSuccess(database to status)
            } catch (e: Throwable) {
                database.close()
                it.onError(e)
            }
        }
                .subscribeOn(Schedulers.single())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        { onConnectSuccess(it, callback) },
                        { callback.onConnectError(it) }
                )
    }

    private fun onConnectSuccess(pair: Pair<MoneyDatabase, VersionStatus>, callback: Callback) {

        val database = pair.first
        val status = pair.second

        when (status) {

            is CurrentVersion -> callback.onConnectSuccess(database)

            is UnsupportedVersion -> {
                database.close()
                callback.onConnectUnsupportedVersion()
            }

            is PendingUpgrades -> if (callback.onConnectPendingUpgrades()) {
                applyUpgrades(database, status, callback)
            } else {
                database.close()
            }
        }
    }

    private fun applyUpgrades(database: MoneyDatabase, upgrades: PendingUpgrades, callback: DatabaseConnector.Callback) {

        Completable.create {
            upgrades.apply()
            it.onComplete()
        }
                .subscribeOn(Schedulers.single())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        { callback.onConnectSuccess(database) },
                        { callback.onConnectError(it) }
                )
    }
}

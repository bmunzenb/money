package com.munzenberger.money.app.database

import com.munzenberger.money.app.ApplicationState
import com.munzenberger.money.core.DatabaseDialect
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.version.CurrentVersion
import com.munzenberger.money.version.PendingUpgrades
import com.munzenberger.money.version.UnsupportedVersion
import com.munzenberger.money.version.VersionStatus
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.sql.DriverManager

class DatabaseConnector {

    interface Callback {

        fun onConnectPendingUpgrades(): Boolean
        fun onConnectUnsupportedVersion()
        fun onConnectError(error: Throwable)
    }

    fun connect(
            name: String? = null,
            driver: String,
            dialect: DatabaseDialect,
            connectionUrl: String,
            user: String? = null,
            password: String? = null,
            callback: Callback
    ) {

        // make sure any previously opened database is closed
        ApplicationState.database = null

        Single.create<Pair<MoneyDatabase, VersionStatus>> {

            Class.forName(driver)

            val connection = DriverManager.getConnection(connectionUrl, user, password)
            val database = MoneyDatabase(connection, dialect, name)

            try {
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

            is CurrentVersion -> ApplicationState.database = database

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
                        { ApplicationState.database = database },
                        { callback.onConnectError(it) }
                )
    }
}

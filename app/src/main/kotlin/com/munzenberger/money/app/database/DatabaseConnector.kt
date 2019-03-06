package com.munzenberger.money.app.database

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.sql.ConnectionQueryExecutor
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
        fun onConnectComplete(database: MoneyDatabase)
        fun onConnectUnsupportedVersion()
        fun onConnectError(error: Throwable)
    }

    fun connect(
            driver: String,
            connectionUrl: String,
            user: String? = null,
            password: String? = null,
            callback: Callback
    ) {

        Single.create<Pair<MoneyDatabase, VersionStatus>> {

            Class.forName(driver)

            val connection = DriverManager.getConnection(connectionUrl, user, password)
            val database = MoneyDatabase(connection)

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

            is CurrentVersion -> callback.onConnectComplete(database)

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
}

private fun applyUpgrades(database: MoneyDatabase, upgrades: PendingUpgrades, callback: DatabaseConnector.Callback) {

    Completable.create {
        upgrades.apply()
        it.onComplete()
    }
            .subscribeOn(Schedulers.single())
            .observeOn(JavaFxScheduler.platform())
            .subscribe(
                    { callback.onConnectComplete(database) },
                    { callback.onConnectError(it) }
            )
}

package com.munzenberger.money.app.database

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.core.ConnectionMoneyDatabase
import com.munzenberger.money.core.DatabaseDialect
import com.munzenberger.money.core.SQLiteDatabaseDialect
import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.CurrentVersion
import com.munzenberger.money.version.PendingUpgrades
import com.munzenberger.money.version.UnsupportedVersion
import com.munzenberger.money.version.VersionStatus
import javafx.concurrent.Task
import java.sql.DriverManager

interface DatabaseConnectorCallbacks {
    fun onCanceled()
    fun onUnsupportedVersion()
    fun onPendingUpgrades(): Boolean
    fun onConnected(database: ObservableMoneyDatabase, isFirstUse: Boolean)
    fun onConnectError(error: Throwable)
}

abstract class DatabaseConnector {

    abstract fun connect(callbacks: DatabaseConnectorCallbacks)

    protected fun connect(
            name: String,
            dialect: DatabaseDialect,
            connectionUrl: String,
            user: String? = null,
            password: String? = null,
            callbacks: DatabaseConnectorCallbacks
    ) {

        val task = object : Task<ObservableMoneyDatabase>() {

            override fun call(): ObservableMoneyDatabase {
                val connection = DriverManager.getConnection(connectionUrl, user, password)

                return ObservableMoneyDatabase(ConnectionMoneyDatabase(name, dialect, connection)).also {
                    when (dialect) {
                        SQLiteDatabaseDialect ->
                            // SQLite requires explicitly enabling foreign key constraints
                            // https://www.sqlite.org/foreignkeys.html#fk_enable
                            it.execute(Query("PRAGMA foreign_keys = ON"))
                    }
                }
            }

            override fun succeeded() {
                onConnectSuccess(value, callbacks)
            }

            override fun failed() {
                callbacks.onConnectError(exception)
            }
        }

        Executors.SINGLE.execute(task)
    }

    private fun onConnectSuccess(database: ObservableMoneyDatabase, callbacks: DatabaseConnectorCallbacks) {

        val task = object : Task<VersionStatus>() {

            override fun call(): VersionStatus {
                return MoneyCoreVersionManager().getVersionStatus(database)
            }

            override fun succeeded() {
                onVersionStatus(database, value, callbacks)
            }

            override fun failed() {
                database.close()
                callbacks.onConnectError(exception)
            }
        }

        Executors.SINGLE.execute(task)
    }

    private fun onVersionStatus(database: ObservableMoneyDatabase, status: VersionStatus, callbacks: DatabaseConnectorCallbacks) {

        when (status) {

            is CurrentVersion ->
                callbacks.onConnected(database, false)

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

        val task = object : Task<Unit>() {

            override fun call() {
                upgrades.apply()
            }

            override fun succeeded() {
                callbacks.onConnected(database, upgrades.isFirstUse)
            }

            override fun failed() {
                database.close()
                callbacks.onConnectError(exception)
            }
        }

        Executors.SINGLE.execute(task)
    }
}

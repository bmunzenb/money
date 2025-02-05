package com.munzenberger.money.app.database

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.core.DatabaseDialect
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.version.getVersionStatus
import com.munzenberger.money.version.VersionStatus
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.concurrent.Task
import java.sql.DriverManager

interface DatabaseConnectorCallbacks {
    fun onCanceled()

    fun onUnsupportedVersion()

    fun onPendingUpgrades(isFirstUse: Boolean): Boolean

    fun onConnected(
        database: ObservableMoneyDatabase,
        isFirstUse: Boolean,
    )

    fun onConnectError(error: Throwable)
}

abstract class DatabaseConnector {
    private val isConnectionInProgress = SimpleBooleanProperty(false)

    val isConnectionInProgressProperty: ReadOnlyBooleanProperty = isConnectionInProgress

    abstract fun connect(callbacks: DatabaseConnectorCallbacks)

    protected fun connect(
        name: String,
        dialect: DatabaseDialect,
        connectionUrl: String,
        user: String? = null,
        password: String? = null,
        callbacks: DatabaseConnectorCallbacks,
    ) {
        val task =
            object : Task<ObservableMoneyDatabase>() {
                override fun call(): ObservableMoneyDatabase {
                    val connection = DriverManager.getConnection(connectionUrl, user, password)
                    val database = MoneyDatabase.open(name, dialect, connection)
                    return ObservableMoneyDatabase(database)
                }

                override fun succeeded() {
                    onConnectSuccess(value, callbacks)
                }

                override fun failed() {
                    callbacks.onConnectError(exception)
                }
            }

        isConnectionInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }

    private fun onConnectSuccess(
        database: ObservableMoneyDatabase,
        callbacks: DatabaseConnectorCallbacks,
    ) {
        val task =
            object : Task<VersionStatus>() {
                override fun call(): VersionStatus = database.getVersionStatus()

                override fun succeeded() {
                    onVersionStatus(database, value, callbacks)
                }

                override fun failed() {
                    database.close()
                    callbacks.onConnectError(exception)
                }
            }

        isConnectionInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }

    private fun onVersionStatus(
        database: ObservableMoneyDatabase,
        status: VersionStatus,
        callbacks: DatabaseConnectorCallbacks,
    ) {
        when (status) {
            is VersionStatus.CurrentVersion ->
                callbacks.onConnected(database, false)

            is VersionStatus.UnsupportedVersion -> {
                database.close()
                callbacks.onUnsupportedVersion()
            }

            is VersionStatus.RequiresUpgrade ->
                when (callbacks.onPendingUpgrades(status.requiresInitialization)) {
                    true -> applyPendingUpgrades(database, status, callbacks)
                    else -> {
                        database.close()
                        callbacks.onCanceled()
                    }
                }
        }
    }

    private fun applyPendingUpgrades(
        database: ObservableMoneyDatabase,
        upgrades: VersionStatus.RequiresUpgrade,
        callbacks: DatabaseConnectorCallbacks,
    ) {
        val task =
            object : Task<Unit>() {
                override fun call() {
                    upgrades.applyUpgrade()
                }

                override fun succeeded() {
                    callbacks.onConnected(database, upgrades.requiresInitialization)
                }

                override fun failed() {
                    database.close()
                    callbacks.onConnectError(exception)
                }
            }

        isConnectionInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }
}

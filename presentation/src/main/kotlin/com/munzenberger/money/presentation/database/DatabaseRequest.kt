package com.munzenberger.money.presentation.database

import com.munzenberger.money.data.api.MoneyRepositoryConnectionResult
import java.io.File

/** A database for [MoneyRepositoryController] to open, as the welcome screen and the menu bar ask for one. */
sealed interface DatabaseRequest {
    /** Creates a new database at [file], replacing any file already there. */
    data class Create(val file: File) : DatabaseRequest

    data class Open(val file: File) : DatabaseRequest

    data object Demo : DatabaseRequest
}

/** Why a [DatabaseRequest] failed, for the UI to map to its own message. */
enum class DatabaseError {
    CreateDatabaseFailed,
    OpenDatabaseFailed,
    OpenDemoFailed,
}

/**
 * Opens the database [request] asks for, closing any open repository first. Returns the error to show if it fails,
 * or `null` once it's open.
 */
suspend fun MoneyRepositoryController.execute(request: DatabaseRequest): DatabaseError? {
    val result = when (request) {
        is DatabaseRequest.Create -> createDatabase(request.file)
        is DatabaseRequest.Open -> openDatabase(request.file)
        DatabaseRequest.Demo -> openDemo()
    }
    return when (result) {
        is MoneyRepositoryConnectionResult.Success -> null
        is MoneyRepositoryConnectionResult.Failure -> request.error
    }
}

private val DatabaseRequest.error: DatabaseError
    get() = when (this) {
        is DatabaseRequest.Create -> DatabaseError.CreateDatabaseFailed
        is DatabaseRequest.Open -> DatabaseError.OpenDatabaseFailed
        DatabaseRequest.Demo -> DatabaseError.OpenDemoFailed
    }

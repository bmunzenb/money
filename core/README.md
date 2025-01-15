# Money Core

This module contains the core entity implementations for Money applications. Use this to store and retrieve entities
from a Money database.

## User Guide

### Connecting to a Money database

All access to a Money database is through a JDBC connection. Establishing a connection requires that you provide a JDBC
connection and a dialect for the database system. Currently supported database dialects are:

1. [SQLite](https://www.sqlite.org/)
2. [H2](https://www.h2database.com/)

To establish a connection to a Money database, call the `MoneyDatabase.open` function:

```kotlin
// acquire a JDBC connection to the database
val connection = DriverManager.getConnection("jdbc:sqlite:/database.money")

val database = MoneyDatabase.open(
    name = "My Money database",
    dialect = SQLiteDatabaseDialect,
    connection = connection
)
```

Note that since the returned instance of `MoneyDatabase` is backed by a single JDBC connection, callers should take care
not to execute multiple queries in parallel.

#### Verify and update the database version

Once connected to a Money database, you must verify the current version of the database schema, and update it if it's
out-of-date.  To check the database version, use an instance of `MoneyDatabaseVersionManager` to retrieve the status of
the database version:

```kotlin
val status = MoneyDatabaseVersionManager().getVersionStatus(database)
```

The resulting status object will be one of:

| Type               | Description                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------|
| `CurrentVersion`   | The Money database is up-to-date and ready to use.                                           |
| `PendingUpgrades`  | The Money database requires updates before it can be used.                                   |
| `UnsupportedVersion` | The version of the Money database is not supported by the version of the core module in use. |

When `PendingUpdgrades` is returned, you can all the `apply` function to apply the pending updates to the database
schema.  `PendingUpgrades` also includes a property that denotes whether this is the first use of the database, which
means the database has not yet been initialized for use with the core module.

*Note that when `PendingUpgrades` is returned and the `isFirstUse` property is `false`, you should request confirmation
from the user before calling `apply` as performing a database upgrade is an irreversible operation.*

Here is an example of handling the result of a Money database version check:

```kotlin
when (status) {
    VersionStatus.CurrentVersion -> { 
        // database is ready to use
    }
    VersionStatus.UnsupportedVersion -> {
        // database is not compatible with this version of the core module
        // close the connection
        database.close()
    }
    is VersionStatus.PendingUpgrades -> {
        if (status.isFirstUse) {
            // database needs to be initialized
            status.apply()
        } else {
            // request confirmation from the user before performing upgrade
            if (requestUserConfirmation()) {
                // user confirmed, apply the updates
                status.apply()
            } else {
                // user declined, close the connection
                database.close()
            }
        }
    }
}
```
package com.munzenberger.money.data.sql

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import java.sql.SQLException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SqlMoneyRepositoryTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        group = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private val directory = Files.createTempDirectory("SqlMoneyRepositoryTest")

    private fun databaseFile(): File = directory.resolve("money.db").toFile()

    @AfterTest
    fun cleanup() {
        directory.toFile().deleteRecursively()
    }

    @Test
    fun `open creates the database file when it does not already exist`() {
        val file = databaseFile()
        assertFalse(file.exists())

        SqlMoneyRepository.open(file)

        assertTrue(file.exists())
    }

    @Test
    fun `open initializes a queryable schema for a new file`() = runTest {
        val repository = SqlMoneyRepository.open(databaseFile())
        assertTrue(repository.accounts.first().isEmpty())
    }

    @Test
    fun `open preserves data across repeated opens of the same file`() = runTest {
        val file = databaseFile()
        val account = Account(name = "Checking", accountType = checking)

        SqlMoneyRepository.open(file).add(account)

        val reopened = SqlMoneyRepository.open(file)
        assertEquals(listOf(account), reopened.accounts.first())
    }

    @Test
    fun `open enables foreign key constraints`() = runTest {
        val repository = SqlMoneyRepository.open(databaseFile())
        val orphan = Account(name = "Orphan", accountType = checking.copy(id = AccountTypeId(-1)))

        assertFailsWith<SQLException> {
            repository.add(orphan)
        }
    }
}

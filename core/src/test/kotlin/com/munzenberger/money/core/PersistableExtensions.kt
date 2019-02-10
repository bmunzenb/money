package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor
import java.util.*

private val random = Random()

fun randomString(length: Int = 50): String {

    val alphabet = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz "

    return (0..length)
            .map { alphabet[random.nextInt(alphabet.length)] }
            .joinTo(StringBuilder(), "").toString()
}

fun Bank.randomize() = this.apply {
    name = randomString()
}

fun AccountType.randomize() = this.apply {
    name = randomString()
    category = AccountType.Category.values().let { it[random.nextInt(it.size)] }
}

fun Account.randomize(executor: QueryExecutor) = this.apply {
    name = randomString()
    number = randomString()
    accountType = AccountType(executor).randomize()
    bank = Bank(executor).randomize()
}

fun Payee.randomize() = this.apply {
    name = randomString()
}

fun Category.randomize(executor: QueryExecutor) = this.apply {
    account = Account(executor).randomize(executor)
    name = randomString()
}

fun Transaction.randomize(executor: QueryExecutor) = this.apply {
    account = Account(executor).randomize(executor)
    payee = Payee(executor).randomize()
    date = Date()
    memo = randomString()
}

fun Transfer.randomize(executor: QueryExecutor) = this.apply {
    category = Category(executor).randomize(executor)
    setTransaction(Transaction(executor).randomize(executor))
    amount = random.nextLong()
    memo = randomString()
}

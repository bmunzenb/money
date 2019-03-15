package com.munzenberger.money.core

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

fun Account.randomize() = this.apply {
    name = randomString()
    number = randomString()
    accountType = AccountType().randomize()
    bank = Bank().randomize()
}

fun Payee.randomize() = this.apply {
    name = randomString()
}

fun Category.randomize() = this.apply {
    account = Account().randomize()
    name = randomString()
}

fun Transaction.randomize() = this.apply {
    account = Account().randomize()
    payee = Payee().randomize()
    date = Date()
    memo = randomString()
}

fun Transfer.randomize() = this.apply {
    category = Category().randomize()
    setTransaction(Transaction().randomize())
    amount = random.nextLong()
    memo = randomString()
}

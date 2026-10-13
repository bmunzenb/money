package com.munzenberger.money.data.api.account

import com.munzenberger.money.data.api.EntityNotFoundException

interface AccountWriter {
    fun add(account: Account)

    /** Updates the stored account with [account]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(account: Account)

    fun removeById(accountId: AccountId)
}

fun AccountWriter.remove(account: Account) {
    removeById(account.id)
}

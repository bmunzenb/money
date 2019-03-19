package com.munzenberger.money.app

interface DatabaseConnectorDelegate {

    fun onCreateDatabase()

    fun onOpenDatabase()

    fun onMemoryDatabase()
}

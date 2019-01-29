package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

class MoneyDatabase(private val executer: QueryExecutor) : QueryExecutor by executer

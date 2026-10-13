package com.munzenberger.money.data.sql

import java.util.logging.Logger

internal val Any.logger: Logger
    get() = Logger.getLogger(this::class.java.name)

package com.munzenberger.money.core

import java.util.logging.Logger

internal val Any.logger: Logger
    get() = Logger.getLogger(this::class.java.name)

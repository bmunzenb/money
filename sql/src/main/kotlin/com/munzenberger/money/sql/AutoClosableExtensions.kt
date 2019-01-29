package com.munzenberger.money.sql

import java.util.logging.Level
import java.util.logging.Logger

fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    val logger = Logger.getLogger(this::class.java.name)
    try {
        return block(this)
    } finally {
        try {
            this.close()
        } catch (e: Exception) {
            logger.log(Level.WARNING, "failed to close resource", e)
        }
    }
}
package com.munzenberger.money.desktop

import java.text.MessageFormat
import java.util.ResourceBundle

val strings: ResourceBundle = ResourceBundle.getBundle("com.munzenberger.money.desktop.strings")

/** Returns the string for [key] with its `{0}`, `{1}`, … placeholders replaced by [args], in [MessageFormat] style. */
fun ResourceBundle.getString(key: String, vararg args: Any): String =
    MessageFormat(getString(key)).format(args)

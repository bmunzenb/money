package com.munzenberger.money.presentation.accounts

/**
 * The account number with all but its last [VISIBLE_ACCOUNT_NUMBER_DIGITS] characters hidden, e.g.
 * "••••1234", or an empty string if there's no number. A number no longer than that is hidden
 * entirely and shows just the mask, so revealing its last characters wouldn't reveal all of it.
 */
fun maskAccountNumber(number: String?): String {
    val trimmed = number?.trim().orEmpty()
    return when {
        trimmed.isEmpty() -> ""
        trimmed.length <= VISIBLE_ACCOUNT_NUMBER_DIGITS -> ACCOUNT_NUMBER_MASK
        else -> ACCOUNT_NUMBER_MASK + trimmed.takeLast(VISIBLE_ACCOUNT_NUMBER_DIGITS)
    }
}

private const val VISIBLE_ACCOUNT_NUMBER_DIGITS = 4
private const val ACCOUNT_NUMBER_MASK = "••••"

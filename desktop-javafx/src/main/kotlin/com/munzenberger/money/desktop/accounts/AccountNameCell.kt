package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.Account
import javafx.geometry.Insets
import javafx.scene.control.Hyperlink
import javafx.scene.control.TableCell

/** Shows an account's name as a link, and a group's or subtotal's name as text. */
internal class AccountNameCell(
    private val onAccountClick: (Account) -> Unit,
) : TableCell<AccountListRow, String>() {
    // Opens the account in the cell's row. It never shows as visited, since the cell reuses it for other accounts.
    private val link = Hyperlink().apply {
        padding = Insets.EMPTY
        setOnAction {
            isVisited = false
            (tableRow?.item as? AccountListRow.AccountRow)?.let { onAccountClick(it.account) }
        }
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        if (!empty && tableRow?.item is AccountListRow.AccountRow) {
            link.text = item
            text = null
            graphic = link
        } else {
            text = if (empty) null else item
            graphic = null
        }
    }
}

package com.munzenberger.money.core.demo

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.data.api.today
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransferEntry
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.cancellation.CancellationException

private val logger = Logger.getLogger(PopulateDemoDataUseCase::class.java.name)

/**
 * Adds a handful of sample banks to the repository, each with a few accounts of varying account types, along
 * with sample categories, payees, and a couple of months of transactions in each account, in a single
 * transaction. Some transactions have a single category or transfer entry, and others are split across several.
 * Transaction dates are relative to today, so the data always looks recent. Meant for a new, empty demo
 * database.
 */
class PopulateDemoDataUseCase {
    suspend operator fun invoke(repository: MoneyRepository): PopulateDemoDataResult =
        try {
            val accountTypes = repository.accountTypes.first().associateBy { it.value }
            val categoryTypes = repository.categoryTypes.first().associateBy { it.value }
            val statuses = repository.transactionStatuses.first().associateBy { it.value }

            val categories = demoCategories.toCategories(categoryTypes)
            val payees = demoPayees.associateWith { Payee(name = it) }
            val banks = demoBanks.map { demoBank ->
                val bank = Bank(name = demoBank.name)
                bank to demoBank.accounts.map { it to it.toAccount(bank, accountTypes) }
            }
            val lookup = DemoLookup(
                categories = categories,
                payees = payees,
                accounts = banks
                    .flatMap { (_, accounts) -> accounts }
                    .associate { (demoAccount, account) -> demoAccount.name to account },
                statuses = statuses,
            )
            val today = LocalDate.today()
            val transactions = banks.flatMap { (_, accounts) ->
                accounts.flatMap { (demoAccount, account) ->
                    demoAccount.transactions.map { it.toEntities(account, today, lookup) }
                }
            }

            repository.transaction {
                categories.values.forEach { add(it) }
                payees.values.forEach { add(it) }
                banks.forEach { (bank, accounts) ->
                    add(bank)
                    accounts.forEach { (_, account) -> add(account) }
                }
                transactions.forEach { (transaction, categoryEntries, transferEntries) ->
                    add(transaction)
                    categoryEntries.forEach { add(it) }
                    transferEntries.forEach { add(it) }
                }
            }
            PopulateDemoDataResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to populate the demo data", e)
            PopulateDemoDataResult.Failure(e)
        }
}

private class DemoBank(val name: String, val accounts: List<DemoAccount>)

/** A sample account; [initialBalance] is in cents, and negative for money owed. */
private class DemoAccount(
    val name: String,
    val number: String,
    val type: AccountTypeConstant,
    val initialBalance: Long,
    val transactions: List<DemoTransaction>,
)

/** A sample category, with the names of its [subcategories], which have the same type. */
private class DemoCategory(
    val name: String,
    val type: CategoryTypeConstant,
    val subcategories: List<String> = emptyList(),
)

/** A sample transaction dated [daysAgo] days before today, in the account that lists it. */
private class DemoTransaction(
    val daysAgo: Int,
    val payee: String?,
    val entries: List<DemoEntry>,
    val number: String?,
    val memo: String?,
)

/**
 * A sample transaction entry; [amount] is in cents, and is how much it changes the balance of the transaction's
 * account by, as in [com.munzenberger.money.data.api.transaction.TransactionEntry.amount].
 */
private sealed interface DemoEntry {
    val amount: Long
    val memo: String?
}

private class DemoCategoryEntry(val category: String, override val amount: Long, override val memo: String?) :
    DemoEntry

/** A transfer to or from the account named [account]. */
private class DemoTransferEntry(val account: String, override val amount: Long, override val memo: String?) :
    DemoEntry

private fun transaction(
    daysAgo: Int,
    payee: String?,
    vararg entries: DemoEntry,
    number: String? = null,
    memo: String? = null,
) = DemoTransaction(daysAgo, payee, entries.toList(), number, memo)

private fun category(name: String, amount: Long, memo: String? = null) = DemoCategoryEntry(name, amount, memo)

private fun transfer(account: String, amount: Long, memo: String? = null) = DemoTransferEntry(account, amount, memo)

/** Finds the entities the sample data refers to by name, failing if one is missing. */
private class DemoLookup(
    private val categories: Map<String, Category>,
    private val payees: Map<String, Payee>,
    private val accounts: Map<String, Account>,
    private val statuses: Map<TransactionStatusConstant, TransactionStatus>,
) {
    fun category(name: String) = checkNotNull(categories[name]) { "No demo category $name" }

    fun payee(name: String) = checkNotNull(payees[name]) { "No demo payee $name" }

    fun account(name: String) = checkNotNull(accounts[name]) { "No demo account $name" }

    fun status(value: TransactionStatusConstant) =
        checkNotNull(statuses[value]) { "No transaction status $value in the repository" }
}

private data class DemoTransactionEntities(
    val transaction: Transaction,
    val categoryEntries: List<CategoryEntry>,
    val transferEntries: List<TransferEntry>,
)

private fun DemoAccount.toAccount(bank: Bank, accountTypes: Map<AccountTypeConstant, AccountType>) =
    Account(
        name = name,
        number = number,
        accountType = checkNotNull(accountTypes[type]) { "No account type $type in the repository" },
        bankId = bank.id,
        initialBalance = Money(initialBalance),
    )

/** Each category, keyed by name, with every parent before its subcategories. */
private fun List<DemoCategory>.toCategories(
    categoryTypes: Map<CategoryTypeConstant, CategoryType>,
): Map<String, Category> =
    buildMap {
        this@toCategories.forEach { demoCategory ->
            val type = checkNotNull(categoryTypes[demoCategory.type]) {
                "No category type ${demoCategory.type} in the repository"
            }
            val parent = Category(name = demoCategory.name, type = type)
            put(parent.name, parent)
            demoCategory.subcategories.forEach { put(it, Category(name = it, parentId = parent.id, type = type)) }
        }
    }

private fun DemoTransaction.toEntities(
    account: Account,
    today: LocalDate,
    lookup: DemoLookup,
): DemoTransactionEntities {
    // Older transactions have been through a statement, so they're further along.
    val status = lookup.status(
        when {
            daysAgo > RECONCILED_AFTER_DAYS -> TransactionStatusConstant.Reconciled
            daysAgo > CLEARED_AFTER_DAYS -> TransactionStatusConstant.Cleared
            else -> TransactionStatusConstant.Unreconciled
        },
    )
    val transaction = Transaction(
        accountId = account.id,
        payeeId = payee?.let { lookup.payee(it).id },
        date = today.minus(daysAgo, DateTimeUnit.DAY),
        number = number,
        memo = memo,
        status = status,
    )
    val categoryEntries = mutableListOf<CategoryEntry>()
    val transferEntries = mutableListOf<TransferEntry>()
    entries.forEachIndexed { index, entry ->
        when (entry) {
            is DemoCategoryEntry -> categoryEntries += CategoryEntry(
                transactionId = transaction.id,
                categoryId = lookup.category(entry.category).id,
                amount = Money(entry.amount),
                memo = entry.memo,
                orderInTransaction = index,
            )
            is DemoTransferEntry -> transferEntries += TransferEntry(
                transactionId = transaction.id,
                accountId = lookup.account(entry.account).id,
                amount = Money(entry.amount),
                memo = entry.memo,
                status = status,
                orderInTransaction = index,
            )
        }
    }
    return DemoTransactionEntities(transaction, categoryEntries, transferEntries)
}

private const val RECONCILED_AFTER_DAYS = 30
private const val CLEARED_AFTER_DAYS = 7

private val demoCategories = listOf(
    DemoCategory("Salary", CategoryTypeConstant.Income),
    DemoCategory("Interest Income", CategoryTypeConstant.Income),
    DemoCategory("Dividends", CategoryTypeConstant.Income),
    DemoCategory("Auto", CategoryTypeConstant.Expense, listOf("Fuel", "Auto Insurance", "Auto Service")),
    DemoCategory("Food", CategoryTypeConstant.Expense, listOf("Groceries", "Dining Out")),
    DemoCategory(
        "Housing", CategoryTypeConstant.Expense, listOf("Mortgage Interest", "Property Tax", "Home Insurance"),
    ),
    DemoCategory("Utilities", CategoryTypeConstant.Expense, listOf("Electric", "Water", "Internet", "Mobile Phone")),
    DemoCategory("Bank Fees", CategoryTypeConstant.Expense),
    DemoCategory("Entertainment", CategoryTypeConstant.Expense),
    DemoCategory("Gifts", CategoryTypeConstant.Expense),
    DemoCategory("Healthcare", CategoryTypeConstant.Expense),
    DemoCategory("Household", CategoryTypeConstant.Expense),
    DemoCategory("Interest Paid", CategoryTypeConstant.Expense),
    DemoCategory("Shopping", CategoryTypeConstant.Expense),
    DemoCategory("Taxes", CategoryTypeConstant.Expense),
    DemoCategory("Travel", CategoryTypeConstant.Expense),
)

private val demoPayees = listOf(
    "Brightline Engineering",
    "Cedar Ridge Schools",
    "Green Valley Market",
    "Megamart",
    "Bluebird Café",
    "Harbor Grill",
    "City Power & Light",
    "Metro Water District",
    "Lightwave Internet",
    "Pinnacle Wireless",
    "Speedy Fuel",
    "Shield Auto Insurance",
    "Main Street Auto Care",
    "Hometown Hardware",
    "Starlight Cinemas",
    "Skyway Airlines",
    "Riverside Family Clinic",
    "First National Bank",
    "Lakeshore Credit Union",
    "Summit Investments",
    "Riverside Savings Bank",
)

/** A split paycheck: gross pay, less withholding and benefits, with part of it moved to savings. */
@Suppress("MagicNumber")
private fun brightlinePaycheck(daysAgo: Int) =
    transaction(
        daysAgo,
        "Brightline Engineering",
        category("Salary", 4_200_00),
        category("Taxes", -980_00, "Federal and state withholding"),
        category("Healthcare", -145_00, "Health insurance premium"),
        transfer("High-Yield Savings", -300_00, "Automatic savings"),
        memo = "Paycheck",
    )

@Suppress("MagicNumber")
private fun cedarRidgePaycheck(daysAgo: Int) =
    transaction(
        daysAgo,
        "Cedar Ridge Schools",
        category("Salary", 2_600_00),
        category("Taxes", -520_00, "Federal and state withholding"),
        transfer("Holiday Savings", -100_00, "Holiday club"),
        memo = "Paycheck",
    )

/** A split mortgage payment: principal to the loan, with interest and escrow to their categories. */
@Suppress("MagicNumber")
private fun mortgagePayment(daysAgo: Int, principal: Long, interest: Long) =
    transaction(
        daysAgo,
        "Riverside Savings Bank",
        transfer("Home Mortgage", -principal, "Principal"),
        category("Mortgage Interest", -interest),
        category("Property Tax", -280_00, "Escrow"),
        category("Home Insurance", -95_00, "Escrow"),
        memo = "Mortgage payment",
    )

private val demoBanks = listOf(
    DemoBank(
        name = "First National Bank",
        accounts = listOf(
            DemoAccount(
                "Everyday Checking", "1001234567", AccountTypeConstant.Checking, 2_345_67,
                listOf(
                    brightlinePaycheck(75),
                    transaction(72, "Green Valley Market", category("Groceries", -142_37)),
                    transaction(68, "City Power & Light", category("Electric", -118_42), number = "1041"),
                    transaction(66, null, transfer("Joint Checking", -2_500_00), memo = "Household budget"),
                    brightlinePaycheck(61),
                    transaction(
                        58, "Megamart",
                        category("Groceries", -86_12),
                        category("Household", -42_50, "Cleaning supplies"),
                        category("Shopping", -64_99, "Jacket"),
                    ),
                    transaction(
                        54, "First National Bank", transfer("Rewards Visa", -1_234_56),
                        memo = "Credit card payment",
                    ),
                    brightlinePaycheck(47),
                    transaction(44, "Bluebird Café", category("Dining Out", -18_75)),
                    transaction(40, "Lightwave Internet", category("Internet", -79_99)),
                    transaction(36, null, transfer("Joint Checking", -2_500_00), memo = "Household budget"),
                    brightlinePaycheck(33),
                    transaction(30, "Metro Water District", category("Water", -54_20), number = "1042"),
                    transaction(26, "Speedy Fuel", category("Fuel", -48_33)),
                    brightlinePaycheck(19),
                    transaction(
                        16, "Riverside Family Clinic", category("Healthcare", -40_00, "Copay"), number = "1043",
                    ),
                    transaction(12, "Pinnacle Wireless", category("Mobile Phone", -65_00)),
                    transaction(6, null, transfer("Joint Checking", -2_500_00), memo = "Household budget"),
                    brightlinePaycheck(5),
                    transaction(3, "Green Valley Market", category("Groceries", -156_88)),
                    transaction(1, "Hometown Hardware", category("Household", -37_42)),
                ),
            ),
            DemoAccount(
                "High-Yield Savings", "1001234568", AccountTypeConstant.Savings, 12_500_00,
                listOf(
                    transaction(60, "First National Bank", category("Interest Income", 41_10)),
                    transaction(31, "First National Bank", category("Interest Income", 42_35)),
                    transaction(20, null, transfer("Holiday Savings", -250_00), memo = "Holiday fund"),
                    transaction(2, "First National Bank", category("Interest Income", 43_02)),
                ),
            ),
            DemoAccount(
                "Rewards Visa", "4111111111111111", AccountTypeConstant.Credit, -1_234_56,
                listOf(
                    transaction(70, "Harbor Grill", category("Dining Out", -86_40)),
                    transaction(65, "Starlight Cinemas", category("Entertainment", -32_00)),
                    transaction(
                        50, "Megamart",
                        category("Shopping", -120_45, "Shoes"),
                        category("Household", -35_18),
                        category("Groceries", -22_10),
                    ),
                    transaction(37, "Speedy Fuel", category("Fuel", -51_20)),
                    transaction(28, "Skyway Airlines", category("Travel", -412_80, "Flights for spring trip")),
                    transaction(21, "Megamart", category("Shopping", 29_99, "Returned item")),
                    transaction(14, "First National Bank", category("Interest Paid", -18_73, "Finance charge")),
                    transaction(6, "Harbor Grill", category("Dining Out", -64_25)),
                ),
            ),
        ),
    ),
    DemoBank(
        name = "Lakeshore Credit Union",
        accounts = listOf(
            DemoAccount(
                "Share Checking", "2002345678", AccountTypeConstant.Checking, 876_54,
                listOf(
                    cedarRidgePaycheck(60),
                    transaction(57, "Lakeshore Credit Union", transfer("Auto Loan", -457_40), memo = "Loan payment"),
                    transaction(
                        52, "Shield Auto Insurance", category("Auto Insurance", -612_00, "6-month premium"),
                        number = "301",
                    ),
                    transaction(
                        41, "Main Street Auto Care",
                        category("Auto Service", -89_95, "Oil change"),
                        category("Auto Service", -42_00, "Tire rotation"),
                        category("Shopping", -18_99, "Wiper blades"),
                    ),
                    cedarRidgePaycheck(32),
                    transaction(29, "Lakeshore Credit Union", transfer("Auto Loan", -457_40), memo = "Loan payment"),
                    transaction(24, "Speedy Fuel", category("Fuel", -44_10)),
                    transaction(10, "Bluebird Café", category("Dining Out", -12_40)),
                    cedarRidgePaycheck(4),
                    transaction(1, "Lakeshore Credit Union", transfer("Auto Loan", -457_40), memo = "Loan payment"),
                ),
            ),
            DemoAccount(
                "Holiday Savings", "2002345679", AccountTypeConstant.Savings, 1_500_00,
                listOf(
                    transaction(45, "Lakeshore Credit Union", category("Interest Income", 1_25)),
                    transaction(15, "Lakeshore Credit Union", category("Interest Income", 1_31)),
                ),
            ),
            DemoAccount(
                "Auto Loan", "2002345680", AccountTypeConstant.Loan, -18_750_00,
                listOf(
                    transaction(57, "Lakeshore Credit Union", category("Interest Paid", -72_40, "Monthly interest")),
                    transaction(29, "Lakeshore Credit Union", category("Interest Paid", -70_30, "Monthly interest")),
                    transaction(1, "Lakeshore Credit Union", category("Interest Paid", -68_19, "Monthly interest")),
                ),
            ),
        ),
    ),
    DemoBank(
        name = "Summit Investments",
        accounts = listOf(
            DemoAccount(
                "Brokerage", "3003456789", AccountTypeConstant.Asset, 45_210_33,
                listOf(
                    transaction(80, "Summit Investments", category("Dividends", 212_48), memo = "Quarterly dividend"),
                    transaction(62, null, transfer("Joint Checking", 1_000_00), memo = "Monthly contribution"),
                    transaction(
                        35, "Summit Investments",
                        category("Dividends", 188_20),
                        category("Interest Income", 6_14, "Cash sweep interest"),
                        category("Bank Fees", -12_95, "Advisory fee"),
                    ),
                    transaction(9, "Summit Investments", category("Dividends", 224_91), memo = "Quarterly dividend"),
                ),
            ),
            DemoAccount(
                "Roth IRA", "3003456790", AccountTypeConstant.Asset, 28_900_00,
                listOf(
                    transaction(78, "Summit Investments", category("Dividends", 96_40)),
                    transaction(48, null, transfer("Brokerage", 7_000_00), memo = "Annual contribution"),
                    transaction(
                        18, "Summit Investments",
                        category("Dividends", 112_75),
                        category("Bank Fees", -25_00, "Account maintenance fee"),
                    ),
                ),
            ),
        ),
    ),
    DemoBank(
        name = "Riverside Savings Bank",
        accounts = listOf(
            DemoAccount(
                "Joint Checking", "4004567890", AccountTypeConstant.Checking, 3_120_45,
                listOf(
                    mortgagePayment(73, principal = 650_00, interest = 900_00),
                    transaction(
                        55, "Green Valley Market",
                        category("Groceries", -98_20),
                        category("Household", -24_15),
                    ),
                    mortgagePayment(43, principal = 653_25, interest = 896_75),
                    transaction(
                        38, "Hometown Hardware",
                        category("Household", -64_18),
                        category("Gifts", -25_00, "Gift card"),
                    ),
                    transaction(22, "Starlight Cinemas", category("Entertainment", -45_50)),
                    mortgagePayment(13, principal = 656_52, interest = 893_48),
                    transaction(7, "Riverside Family Clinic", category("Healthcare", -25_00, "Copay"), number = "2216"),
                ),
            ),
            DemoAccount(
                "12-Month CD", "4004567891", AccountTypeConstant.Savings, 10_000_00,
                listOf(
                    transaction(60, "Riverside Savings Bank", category("Interest Income", 37_50)),
                    transaction(30, "Riverside Savings Bank", category("Interest Income", 37_65)),
                    transaction(1, "Riverside Savings Bank", category("Interest Income", 37_80)),
                ),
            ),
            DemoAccount(
                "Home Mortgage", "4004567892", AccountTypeConstant.Loan, -245_000_00,
                listOf(
                    transaction(
                        66, "Riverside Savings Bank", category("Property Tax", -214_60, "Escrow shortage"),
                    ),
                    transaction(
                        25, "Riverside Savings Bank", transfer("Joint Checking", 500_00),
                        memo = "Extra principal payment",
                    ),
                ),
            ),
        ),
    ),
)

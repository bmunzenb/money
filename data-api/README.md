# data-api

The repository interfaces and data models for Money, implemented by [/data-sql](../data-sql/src).

### Entity relationships

Attributes use the models' property names. Properties that hold another entity (such as `Account.accountType`)
are shown as a foreign key to it, and `?` marks a nullable property. `Money` is an amount in whole fractional units
(e.g. cents) with its currency.

```mermaid
erDiagram
    AccountClass ||--o{ AccountType : "classifies"
    AccountType ||--o{ Account : "types"
    Bank |o--o{ Account : "holds"
    Account ||--o{ Statement : "has"
    Account ||--o{ Transaction : "has"
    Payee |o--o{ Transaction : "is paid by"
    TransactionStatus ||--o{ Transaction : "describes"
    Transaction ||--o{ CategoryEntry : "is split into"
    Transaction ||--o{ TransferEntry : "is split into"
    CategoryType ||--o{ Category : "types"
    Category |o--o{ Category : "is the parent of"
    Category ||--o{ CategoryEntry : "categorizes"
    Account ||--o{ TransferEntry : "is transferred to or from by"
    TransactionStatus ||--o{ TransferEntry : "describes"

    AccountClass {
        Long id PK
        AccountClassConstant value "Assets, Liabilities"
    }
    AccountType {
        Long id PK
        Long accountClass FK
        AccountTypeConstant value "Savings, Checking, Credit, Loan, Asset, Cash"
    }
    Bank {
        Uuid id PK
        String name
        String memo "?"
    }
    Account {
        Uuid id PK
        String name
        String number "?"
        Long accountType FK
        Uuid bankId FK "?"
        Money initialBalance
        String memo "?"
        Boolean isClosed
    }
    Statement {
        Uuid id PK
        Uuid accountId FK
        LocalDate closingDate
        Money startingBalance
        Money endingBalance
        Boolean isReconciled
    }
    Payee {
        Uuid id PK
        String name
        String memo "?"
    }
    TransactionStatus {
        Long id PK
        TransactionStatusConstant value "Unreconciled, Cleared, Reconciled"
    }
    Transaction {
        Uuid id PK
        Uuid accountId FK
        Uuid payeeId FK "?"
        LocalDate date
        String number "?"
        String memo "?"
        Long status FK
    }
    CategoryType {
        Long id PK
        CategoryTypeConstant value "Income, Expense"
    }
    Category {
        Uuid id PK
        String name
        Uuid parentId FK "?"
        Long type FK
        String memo "?"
    }
    CategoryEntry {
        Uuid id PK
        Uuid transactionId FK
        Uuid categoryId FK
        Money amount
        String memo "?"
        Int orderInTransaction
    }
    TransferEntry {
        Uuid id PK
        Uuid transactionId FK
        Uuid accountId FK
        Money amount
        String number "?"
        String memo "?"
        Long status FK
        Int orderInTransaction
    }
```

A transaction's entries (`CategoryEntry` and `TransferEntry`, both `TransactionEntry`s) each change the balance of
the transaction's account by their `amount`. A `TransferEntry` also changes the balance of its own `accountId` by the
negation of its `amount`.

`RegisterEntry` isn't stored: it's a read model that `RegisterRepository` builds from these entities for an account's
register, with a running balance.

package com.imran.pocketledger.model

import java.math.BigDecimal
import java.time.LocalDate

enum class Category(val label: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other"),
}

/**
 * A value class: type-safe at compile time (you can't pass a random Long where an ExpenseId is expected),
 * but at runtime it's just a Long, so it costs nothing extra.
 */
@JvmInline
value class ExpenseId(val value: Long) {
    companion object {
        /** Placeholder for an expense that hasn't been saved yet; the repository assigns the real id. */
        val NEW = ExpenseId(0)
    }
}

data class Expense(
    val id: ExpenseId,
    val title: String,
    val amount: BigDecimal, // never Double for money, same reason as in Java
    val category: Category,
    val date: LocalDate,
)

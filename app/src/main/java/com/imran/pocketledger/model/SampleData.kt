package com.imran.pocketledger.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

val sampleExpenses = listOf(
    // November 2026
    Expense(id = ExpenseId(1), title = "Groceries", amount = BigDecimal("40.71"), category = Category.FOOD, date = LocalDate.of(2026, 11, 12)),
    Expense(id = ExpenseId(3), title = "Bus pass", amount = BigDecimal("30.00"), category = Category.TRANSPORT, date = LocalDate.of(2026, 11, 2)),
    Expense(id = ExpenseId(4), title = "Electricity bill", amount = BigDecimal("62.35"), category = Category.BILLS, date = LocalDate.of(2026, 11, 8)),
    Expense(id = ExpenseId(5), title = "Cinema", amount = BigDecimal("14.50"), category = Category.ENTERTAINMENT, date = LocalDate.of(2026, 11, 14)),

    // October 2026
    Expense(id = ExpenseId(2), title = "Shirt", amount = BigDecimal("25.00"), category = Category.SHOPPING, date = LocalDate.of(2026, 10, 4)),
    Expense(id = ExpenseId(6), title = "Lunch with team", amount = BigDecimal("18.90"), category = Category.FOOD, date = LocalDate.of(2026, 10, 17)),
    Expense(id = ExpenseId(7), title = "Internet", amount = BigDecimal("45.00"), category = Category.BILLS, date = LocalDate.of(2026, 10, 1)),
    Expense(id = ExpenseId(8), title = "Taxi", amount = BigDecimal("12.40"), category = Category.TRANSPORT, date = LocalDate.of(2026, 10, 23)),

    // September 2026
    Expense(id = ExpenseId(9), title = "Headphones", amount = BigDecimal("79.99"), category = Category.SHOPPING, date = LocalDate.of(2026, 9, 20)),
    Expense(id = ExpenseId(10), title = "Coffee beans", amount = BigDecimal("11.25"), category = Category.FOOD, date = LocalDate.of(2026, 9, 9)),
    Expense(id = ExpenseId(11), title = "Gift wrap", amount = BigDecimal("3.80"), category = Category.OTHER, date = LocalDate.of(2026, 9, 28)),
)

fun groupByMonth(expenses: List<Expense>): Map<YearMonth, List<Expense>> =
    expenses
        .sortedByDescending { it.date }
        .groupBy { YearMonth.from(it.date) }

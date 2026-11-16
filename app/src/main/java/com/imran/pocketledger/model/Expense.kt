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

data class Expense(
    val id: Long,
    val title: String,
    val amount: BigDecimal, // never Double for money, same reason as in Java
    val category: Category,
    val date: LocalDate,
)

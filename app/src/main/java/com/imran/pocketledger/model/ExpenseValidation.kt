package com.imran.pocketledger.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Parses user input like "12.50" into a money amount.
 * Returns null if it isn't a number, is zero or negative, or has more than 2 decimals.
 */
fun String.toAmountOrNull(): BigDecimal? =
    trim()
        .toBigDecimalOrNull()
        ?.takeIf { it > BigDecimal.ZERO && it.scale() <= 2 }

sealed interface ExpenseValidation {
    data class Valid(val expense: Expense) : ExpenseValidation
    data class Invalid(val titleError: String?, val amountError: String?) : ExpenseValidation
}

fun validateExpense(
    id: Long,
    title: String,
    amountText: String,
    category: Category,
    date: LocalDate,
): ExpenseValidation {
    val trimmedTitle = title.trim()
    val amount = amountText.toAmountOrNull()

    val titleError = if (trimmedTitle.isEmpty()) "Title is required" else null
    val amountError = when {
        amountText.isBlank() -> "Amount is required"
        amount == null -> "Enter a positive amount with at most 2 decimals"
        else -> null
    }

    return if (amount != null && titleError == null) {
        ExpenseValidation.Valid(
            Expense(id = id, title = trimmedTitle, amount = amount, category = category, date = date)
        )
    } else {
        ExpenseValidation.Invalid(titleError = titleError, amountError = amountError)
    }
}

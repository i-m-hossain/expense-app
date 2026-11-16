package com.imran.pocketledger.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class ExpenseValidationTest {

    @Test
    fun `toAmountOrNull parses valid amounts`() {
        assertEquals(BigDecimal("12.50"), "12.50".toAmountOrNull())
        assertEquals(BigDecimal("7"), " 7 ".toAmountOrNull())
    }

    @Test
    fun `toAmountOrNull rejects bad input`() {
        listOf("", "abc", "0", "-5", "12.345").forEach { input ->
            assertNull("expected null for '$input'", input.toAmountOrNull())
        }
    }

    @Test
    fun `validateExpense returns Valid with trimmed title`() {
        val result = validateExpense(1, "  Lunch  ", "9.99", Category.FOOD, LocalDate.of(2026, 11, 1))

        assertTrue(result is ExpenseValidation.Valid)
        assertEquals("Lunch", (result as ExpenseValidation.Valid).expense.title)
    }

    @Test
    fun `validateExpense reports both errors`() {
        val result = validateExpense(1, " ", "abc", Category.FOOD, LocalDate.of(2026, 11, 1))

        assertEquals(
            ExpenseValidation.Invalid(
                titleError = "Title is required",
                amountError = "Enter a positive amount with at most 2 decimals",
            ),
            result,
        )
    }
}

package com.imran.pocketledger.navigation

import kotlinx.serialization.Serializable

// Type-safe routes: each screen is a Kotlin type instead of a URL string like "expense/42".
// @Serializable lets Navigation save them in the back stack (and restore them after process death).
// Arguments are plain Longs: Navigation knows how to save those. Screens wrap them in ExpenseId.

@Serializable
data object ExpenseList

@Serializable
data class ExpenseDetail(val id: Long)

/** One form for both add and edit: `expenseId = null` means "new expense". */
@Serializable
data class ExpenseForm(val expenseId: Long? = null)

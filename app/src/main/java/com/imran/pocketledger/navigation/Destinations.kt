package com.imran.pocketledger.navigation

import kotlinx.serialization.Serializable

// Type-safe routes: each screen is a Kotlin type instead of a URL string like "add-expense".
// @Serializable lets Navigation save them in the back stack (and restore them after process death).
// Screens that need arguments (e.g. an expense id) will be `data class`es; these have none, so `data object`.

@Serializable
data object ExpenseList

@Serializable
data object AddExpense

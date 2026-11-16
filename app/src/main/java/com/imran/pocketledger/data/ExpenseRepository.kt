package com.imran.pocketledger.data

import com.imran.pocketledger.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * The only way the rest of the app reads or writes expenses.
 * M3: in memory. M6: Room. M7: Room + network. Callers never change.
 */
interface ExpenseRepository {
    /** Emits the full list now, and again every time it changes. */
    val expenses: Flow<List<Expense>>

    /** Saves a new expense. The repository assigns the id, so callers can pass 0. */
    suspend fun add(expense: Expense)
}

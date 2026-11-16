package com.imran.pocketledger.data

import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.ExpenseId
import kotlinx.coroutines.flow.Flow

/**
 * The only way the rest of the app reads or writes expenses.
 * M3: in memory. M6: Room. M7: Room + network. Callers never change.
 */
interface ExpenseRepository {
    /** Emits the full list now, and again every time it changes. */
    val expenses: Flow<List<Expense>>

    /** One expense, or null if it doesn't exist (e.g. it was deleted). */
    suspend fun get(id: ExpenseId): Expense?

    /** Saves a new expense. The repository assigns the id, so callers pass [ExpenseId.NEW]. */
    suspend fun add(expense: Expense)

    /** Replaces the expense with the same id. */
    suspend fun update(expense: Expense)

    suspend fun delete(id: ExpenseId)

    /** Puts a deleted expense back with its original id (used by Undo). */
    suspend fun restore(expense: Expense)
}

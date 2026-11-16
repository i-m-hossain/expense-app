package com.imran.pocketledger.data

import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.ExpenseId
import com.imran.pocketledger.model.sampleExpenses
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

class InMemoryExpenseRepository(
    initial: List<Expense> = sampleExpenses,
) : ExpenseRepository {

    private val data = MutableStateFlow(initial)

    // Ids only ever go up, so a deleted id is never reused (otherwise Undo could clash with a new expense).
    private val nextId = AtomicLong((initial.maxOfOrNull { it.id.value } ?: 0L) + 1)

    // Fake 500 ms of "loading" so you can see the Loading state. Room/network will be real later.
    override val expenses: Flow<List<Expense>> = data.onStart { delay(500.milliseconds) }

    override suspend fun get(id: ExpenseId): Expense? {
        delay(200.milliseconds)
        return data.value.find { it.id == id }
    }

    override suspend fun add(expense: Expense) {
        delay(300.milliseconds) // pretend this is a disk/network write
        val saved = expense.copy(id = ExpenseId(nextId.getAndIncrement()))
        data.update { current -> current + saved }
    }

    override suspend fun update(expense: Expense) {
        delay(300.milliseconds)
        data.update { current -> current.map { if (it.id == expense.id) expense else it } }
    }

    override suspend fun delete(id: ExpenseId) {
        data.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun restore(expense: Expense) {
        data.update { current -> if (current.any { it.id == expense.id }) current else current + expense }
    }
}

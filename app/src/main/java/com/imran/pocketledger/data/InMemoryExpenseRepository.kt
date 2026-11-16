package com.imran.pocketledger.data

import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.sampleExpenses
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds

class InMemoryExpenseRepository(
    initial: List<Expense> = sampleExpenses,
) : ExpenseRepository {

    private val data = MutableStateFlow(initial)

    // Fake 500 ms of "loading" so you can see the Loading state. Room/network will be real later.
    override val expenses: Flow<List<Expense>> = data.onStart { delay(500.milliseconds) }

    override suspend fun add(expense: Expense) {
        delay(300.milliseconds) // pretend this is a disk/network write
        data.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1
            current + expense.copy(id = nextId)
        }
    }
}

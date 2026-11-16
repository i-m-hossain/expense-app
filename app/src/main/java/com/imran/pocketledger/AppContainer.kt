package com.imran.pocketledger

import com.imran.pocketledger.data.ExpenseRepository
import com.imran.pocketledger.data.InMemoryExpenseRepository

/**
 * Hand-made dependency container: one shared instance of each dependency for the whole app.
 * `object` = a singleton (like a Spring @Bean with default scope). Hilt replaces this in M5.
 */
object AppContainer {
    val expenseRepository: ExpenseRepository by lazy { InMemoryExpenseRepository() }
}

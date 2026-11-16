package com.imran.pocketledger.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imran.pocketledger.data.ExpenseRepository
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.groupByMonth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth

/** Everything the list screen can show. The screen just renders whichever one it gets. */
sealed interface ExpenseListUiState {
    data object Loading : ExpenseListUiState
    data object Empty : ExpenseListUiState
    data class Success(val total: BigDecimal, val months: List<MonthSection>) : ExpenseListUiState
    data class Error(val message: String) : ExpenseListUiState
}

data class MonthSection(val month: YearMonth, val expenses: List<Expense>)

sealed interface ExpenseListEffect {
    /** Show "Deleted … / Undo". Carries the whole expense so Undo can put it back. */
    data class ShowUndoDelete(val expense: Expense) : ExpenseListEffect
}

class ExpenseListViewModel(
    private val repository: ExpenseRepository,
) : ViewModel() {

    // Private writable state, public read-only view: the Kotlin "backing property" pattern.
    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ExpenseListEffect>(Channel.BUFFERED)
    val effects: Flow<ExpenseListEffect> = _effects.receiveAsFlow()

    init {
        // viewModelScope cancels this automatically when the ViewModel is destroyed.
        viewModelScope.launch {
            repository.expenses
                .catch { e -> _uiState.value = ExpenseListUiState.Error(e.message ?: "Couldn't load expenses") }
                .collect { expenses -> _uiState.value = expenses.toUiState() }
        }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense.id)
            _effects.send(ExpenseListEffect.ShowUndoDelete(expense))
        }
    }

    fun undoDelete(expense: Expense) {
        viewModelScope.launch { repository.restore(expense) }
    }
}

private fun List<Expense>.toUiState(): ExpenseListUiState =
    if (isEmpty()) {
        ExpenseListUiState.Empty
    } else {
        ExpenseListUiState.Success(
            total = sumOf { it.amount },
            months = groupByMonth(this).map { (month, items) -> MonthSection(month, items) },
        )
    }

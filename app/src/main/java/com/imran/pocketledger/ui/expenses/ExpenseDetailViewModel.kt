package com.imran.pocketledger.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imran.pocketledger.data.ExpenseRepository
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.ExpenseId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExpenseDetailUiState {
    data object Loading : ExpenseDetailUiState
    data class Found(val expense: Expense) : ExpenseDetailUiState
    data object NotFound : ExpenseDetailUiState
}

class ExpenseDetailViewModel(
    expenseId: ExpenseId,
    repository: ExpenseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseDetailUiState>(ExpenseDetailUiState.Loading)
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    init {
        // Watch the whole list so the detail screen updates by itself after an edit.
        viewModelScope.launch {
            repository.expenses.collect { expenses ->
                val expense = expenses.find { it.id == expenseId }
                _uiState.value = if (expense != null) {
                    ExpenseDetailUiState.Found(expense)
                } else {
                    ExpenseDetailUiState.NotFound
                }
            }
        }
    }
}

package com.imran.pocketledger.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imran.pocketledger.data.ExpenseRepository
import com.imran.pocketledger.model.Category
import com.imran.pocketledger.model.ExpenseValidation
import com.imran.pocketledger.model.validateExpense
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddExpenseUiState(
    val title: String = "",
    val amountText: String = "",
    val category: Category = Category.FOOD,
    val date: LocalDate = LocalDate.now(),
    val titleError: String? = null,
    val amountError: String? = null,
    val isSaving: Boolean = false,
)

/** Things the user did. Like Redux actions. */
sealed interface AddExpenseEvent {
    data class TitleChanged(val value: String) : AddExpenseEvent
    data class AmountChanged(val value: String) : AddExpenseEvent
    data class CategoryChanged(val value: Category) : AddExpenseEvent
    data class DateChanged(val value: LocalDate) : AddExpenseEvent
    data object Save : AddExpenseEvent
    data object Cancel : AddExpenseEvent
}

/** One-off things the UI must do exactly once (not state you'd redraw). */
sealed interface AddExpenseEffect {
    data object Close : AddExpenseEffect
}

class AddExpenseViewModel(
    private val repository: ExpenseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AddExpenseEffect>(Channel.BUFFERED)
    val effects: Flow<AddExpenseEffect> = _effects.receiveAsFlow()

    // Errors only appear after the first Save attempt.
    private var submitted = false

    fun onEvent(event: AddExpenseEvent) {
        when (event) {
            is AddExpenseEvent.TitleChanged -> updateForm { copy(title = event.value) }
            is AddExpenseEvent.AmountChanged -> updateForm { copy(amountText = event.value) }
            is AddExpenseEvent.CategoryChanged -> updateForm { copy(category = event.value) }
            is AddExpenseEvent.DateChanged -> updateForm { copy(date = event.value) }
            AddExpenseEvent.Save -> save()
            AddExpenseEvent.Cancel -> cancel()
        }
    }

    /** `change` is a lambda with receiver: inside it, `this` is the current state, so `copy(...)` works directly. */
    private fun updateForm(change: AddExpenseUiState.() -> AddExpenseUiState) {
        _uiState.update { it.change().withErrors() }
    }

    private fun save() {
        if (_uiState.value.isSaving) return // ignore double taps
        submitted = true

        when (val result = _uiState.value.validate()) {
            is ExpenseValidation.Invalid -> _uiState.update { it.withErrors() }
            is ExpenseValidation.Valid -> viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                repository.add(result.expense) // suspends without blocking the UI
                close()
            }
        }
    }

    private fun cancel() {
        // Once a save has started we let it finish; otherwise it would close the form a second time later.
        if (_uiState.value.isSaving) return
        close()
    }

    private fun close() {
        submitted = false
        _uiState.value = AddExpenseUiState() // fresh, empty form for next time
        _effects.trySend(AddExpenseEffect.Close)
    }

    private fun AddExpenseUiState.validate(): ExpenseValidation =
        validateExpense(id = 0L, title = title, amountText = amountText, category = category, date = date)

    private fun AddExpenseUiState.withErrors(): AddExpenseUiState {
        if (!submitted) return this
        return when (val result = validate()) {
            is ExpenseValidation.Valid -> copy(titleError = null, amountError = null)
            is ExpenseValidation.Invalid -> copy(titleError = result.titleError, amountError = result.amountError)
        }
    }
}

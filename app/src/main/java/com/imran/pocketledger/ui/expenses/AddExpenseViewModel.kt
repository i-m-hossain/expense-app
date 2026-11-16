package com.imran.pocketledger.ui.expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imran.pocketledger.data.ExpenseRepository
import com.imran.pocketledger.model.Category
import com.imran.pocketledger.model.ExpenseId
import com.imran.pocketledger.model.ExpenseValidation
import com.imran.pocketledger.model.validateExpense
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddExpenseUiState(
    val title: String = "",
    val amountText: String = "",
    val category: Category = Category.FOOD,
    val date: LocalDate = LocalDate.now(),
    val titleError: String? = null,
    val amountError: String? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
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

/**
 * Handles both "add" ([expenseId] = null) and "edit" ([expenseId] = the expense being edited).
 *
 * The draft is mirrored into [savedStateHandle], which Android writes to disk when it kills the app
 * in the background (process death) and hands back when the user returns.
 */
class AddExpenseViewModel(
    private val expenseId: ExpenseId?,
    private val savedStateHandle: SavedStateHandle,
    private val repository: ExpenseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState(isEditing = expenseId != null))
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AddExpenseEffect>(Channel.BUFFERED)
    val effects: Flow<AddExpenseEffect> = _effects.receiveAsFlow()

    // Errors only appear after the first Save attempt.
    // A property with a custom getter/setter: reads and writes go straight to SavedStateHandle.
    private var submitted: Boolean
        get() = savedStateHandle[KEY_SUBMITTED] ?: false
        set(value) {
            savedStateHandle[KEY_SUBMITTED] = value
        }

    init {
        val draft = restoreDraft()
        when {
            draft != null -> _uiState.value = draft.withErrors() // came back after process death
            expenseId != null -> loadExpense(expenseId) // first time opening "edit"
        }
    }

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
        val newState = _uiState.updateAndGet { it.change().withErrors() }
        saveDraft(newState)
    }

    private fun loadExpense(id: ExpenseId) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val expense = repository.get(id)
            if (expense == null) {
                // Deleted while we weren't looking: nothing to edit.
                _effects.trySend(AddExpenseEffect.Close)
                return@launch
            }
            val loaded = _uiState.updateAndGet {
                it.copy(
                    title = expense.title,
                    amountText = expense.amount.toPlainString(),
                    category = expense.category,
                    date = expense.date,
                    isLoading = false,
                )
            }
            saveDraft(loaded)
        }
    }

    private fun save() {
        if (_uiState.value.isSaving || _uiState.value.isLoading) return // ignore double taps
        submitted = true

        when (val result = _uiState.value.validate()) {
            is ExpenseValidation.Invalid -> _uiState.update { it.withErrors() }
            is ExpenseValidation.Valid -> viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                if (expenseId == null) {
                    repository.add(result.expense)
                } else {
                    repository.update(result.expense)
                }
                close()
            }
        }
    }

    private fun cancel() {
        // Once a save has started we let it finish; otherwise it would close the form a second time later.
        if (_uiState.value.isSaving) return
        close()
    }

    // No need to reset the form: Navigation destroys this ViewModel when the screen is popped.
    private fun close() {
        _effects.trySend(AddExpenseEffect.Close)
    }

    private fun saveDraft(state: AddExpenseUiState) {
        savedStateHandle[KEY_TITLE] = state.title
        savedStateHandle[KEY_AMOUNT] = state.amountText
        savedStateHandle[KEY_CATEGORY] = state.category.name
        savedStateHandle[KEY_DATE] = state.date.toString() // ISO "2026-11-16": simple and safe to store
    }

    private fun restoreDraft(): AddExpenseUiState? {
        val title = savedStateHandle.get<String>(KEY_TITLE) ?: return null // nothing saved yet
        return _uiState.value.copy(
            title = title,
            amountText = savedStateHandle[KEY_AMOUNT] ?: "",
            category = savedStateHandle.get<String>(KEY_CATEGORY)?.let(Category::valueOf) ?: Category.FOOD,
            date = savedStateHandle.get<String>(KEY_DATE)?.let(LocalDate::parse) ?: LocalDate.now(),
        )
    }

    private fun AddExpenseUiState.validate(): ExpenseValidation =
        validateExpense(
            id = expenseId ?: ExpenseId.NEW,
            title = title,
            amountText = amountText,
            category = category,
            date = date,
        )

    private fun AddExpenseUiState.withErrors(): AddExpenseUiState {
        if (!submitted) return this
        return when (val result = validate()) {
            is ExpenseValidation.Valid -> copy(titleError = null, amountError = null)
            is ExpenseValidation.Invalid -> copy(titleError = result.titleError, amountError = result.amountError)
        }
    }

    private companion object {
        const val KEY_TITLE = "draft_title"
        const val KEY_AMOUNT = "draft_amount"
        const val KEY_CATEGORY = "draft_category"
        const val KEY_DATE = "draft_date"
        const val KEY_SUBMITTED = "draft_submitted"
    }
}

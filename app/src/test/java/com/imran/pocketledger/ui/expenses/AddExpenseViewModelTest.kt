package com.imran.pocketledger.ui.expenses

import com.imran.pocketledger.MainDispatcherRule
import com.imran.pocketledger.data.ExpenseRepository
import com.imran.pocketledger.model.Expense
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** A fake (not a mock): a tiny real implementation we fully control. */
private class FakeExpenseRepository : ExpenseRepository {
    val saved = mutableListOf<Expense>()

    /** If set, add() waits until the test completes it, so we can act "during" a save. */
    var gate: CompletableDeferred<Unit>? = null

    override val expenses: Flow<List<Expense>> = MutableStateFlow(emptyList())

    override suspend fun add(expense: Expense) {
        gate?.await()
        saved += expense
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeExpenseRepository()
    private val viewModel = AddExpenseViewModel(repository)

    private fun fillValidForm() {
        viewModel.onEvent(AddExpenseEvent.TitleChanged("Lunch"))
        viewModel.onEvent(AddExpenseEvent.AmountChanged("12.50"))
    }

    @Test
    fun `errors are hidden until the first save attempt`() {
        viewModel.onEvent(AddExpenseEvent.AmountChanged("abc"))

        assertEquals(null, viewModel.uiState.value.amountError)
    }

    @Test
    fun `saving an invalid form shows errors and saves nothing`() {
        viewModel.onEvent(AddExpenseEvent.Save)

        val state = viewModel.uiState.value
        assertEquals("Title is required", state.titleError)
        assertEquals("Amount is required", state.amountError)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `saving a valid form stores it, resets the form and closes once`() = runTest {
        val effects = mutableListOf<AddExpenseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }

        fillValidForm()
        viewModel.onEvent(AddExpenseEvent.Save)

        assertEquals("Lunch", repository.saved.single().title)
        assertEquals(AddExpenseUiState().title, viewModel.uiState.value.title)
        assertEquals(listOf(AddExpenseEffect.Close), effects)
    }

    @Test
    fun `cancel while saving is ignored, so the form closes only once`() = runTest {
        val effects = mutableListOf<AddExpenseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        fillValidForm()
        viewModel.onEvent(AddExpenseEvent.Save)
        assertTrue(viewModel.uiState.value.isSaving)

        viewModel.onEvent(AddExpenseEvent.Cancel) // user taps back mid-save
        assertTrue(effects.isEmpty())

        gate.complete(Unit) // the save finishes
        advanceUntilIdle()

        assertEquals(listOf(AddExpenseEffect.Close), effects)
        assertEquals(1, repository.saved.size)
    }
}
